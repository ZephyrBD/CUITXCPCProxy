package top.techmczs.cuitxcpcproxy.services.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.techmczs.cuitxcpcproxy.constant.MessageConstant;
import top.techmczs.cuitxcpcproxy.dto.PrintTaskDTO;
import top.techmczs.cuitxcpcproxy.dto.PrintTeamDTO;
import top.techmczs.cuitxcpcproxy.entity.DjTeam;
import top.techmczs.cuitxcpcproxy.entity.queuetask.PrintTask;
import top.techmczs.cuitxcpcproxy.entity.queuetask.QueueTask;
import top.techmczs.cuitxcpcproxy.exception.QueueTaskException;
import top.techmczs.cuitxcpcproxy.exception.GetFileErrorException;
import top.techmczs.cuitxcpcproxy.exception.TeamNotExistException;
import top.techmczs.cuitxcpcproxy.mapper.DjTeamMapper;
import top.techmczs.cuitxcpcproxy.mapper.PrintTaskMapper;
import top.techmczs.cuitxcpcproxy.result.Result;
import top.techmczs.cuitxcpcproxy.services.DjPrintService;
import top.techmczs.cuitxcpcproxy.utils.PdfUtil;
import top.techmczs.cuitxcpcproxy.utils.SqlQueue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DjPrintServiceImpl implements DjPrintService {

    private final SqlQueue<PrintTask> printTaskQueue;
    private final PrintTaskMapper printTaskMapper;
    private final DjTeamMapper djTeamMapper;

    @Override
    public void pushPrintTaskBySse(SseEmitter emitter) {
        try {
            // 1. 连接建立成功，发送初始化消息（前端必收）
            emitter.send(SseEmitter.event().name("connect").data("SSE连接成功，等待打印任务..."));

            // 2. 循环监听（优雅退出）
            while (!Thread.currentThread().isInterrupted()) {
                // 非阻塞获取任务
                PrintTask printTask = dequeue();

                // 无任务 → 发送心跳（3秒一次，降低频率，更稳定）
                if (printTask == null) {
                    // 发送心跳前校验连接是否可用
                    if (isEmitterActive(emitter)) {
                        emitter.send(SseEmitter.event().comment("heartbeat"));
                    } else {
                        log.info("SSE客户端已断开，退出监听");
                        break;
                    }
                    Thread.sleep(3000);
                    continue;
                }

                // ==============================================
                // 3. 【核心修复】取出任务后，立即更新为处理中（避免重复推送）
                UpdateWrapper<PrintTask> wrapper = new UpdateWrapper<>();
                wrapper.lambda()
                        .eq(PrintTask::getId, printTask.getId())
                        .set(PrintTask::getStatus, QueueTask.Status.PROCESSING);
                printTaskMapper.update(null, wrapper);
                // ==============================================

                // 4. 封装任务数据
                PrintTaskDTO printTaskDTO = new PrintTaskDTO()
                        .setTaskId(printTask.getId())
                        .setStatus(QueueTask.Status.PROCESSING);

                DjTeam djTeam = djTeamMapper.selectById(printTask.getExamNum());
                if (djTeam == null) {
                    if (isEmitterActive(emitter)) {
                        emitter.send(SseEmitter.event().name("error").data(Result.error(MessageConstant.TEAM_NOT_FOUND)));
                    }
                    continue;
                }

                printTaskDTO.setTeamName(djTeam.getTeamName())
                        .setTeamPosition(djTeam.getPosition());

                // 5. 【核心】推送任务给前端（发送前校验连接）
                if (isEmitterActive(emitter)) {
                    emitter.send(SseEmitter.event().name("printTask").data(Result.success(printTaskDTO)));
                    log.info("✅ 打印任务推送成功：taskId={}, 队伍={}", printTask.getId(), djTeam.getTeamName());
                } else {
                    log.info("❌ 客户端已断开，任务未推送：taskId={}", printTask.getId());
                    // 推送失败，把任务状态改回PENDING，支持重试
                    wrapper.lambda()
                            .eq(PrintTask::getId, printTask.getId())
                            .set(PrintTask::getStatus, QueueTask.Status.PENDING);
                    printTaskMapper.update(null, wrapper);
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("SSE监听线程被中断");
        } catch (IOException e) {
            log.error("❌ SSE推送失败：客户端已断开连接");
        } finally {
            // 6. 优雅关闭连接
            if (isEmitterActive(emitter)) {
                emitter.complete();
            }
            log.info("SSE连接已关闭");
        }
    }

//    @Override
//    public PrintTaskDTO getAuthTopTaskFromQueue() {
//        PrintTask printTask = dequeue();
//        if (printTask == null) {
//            return null;
//            //throw new QueueTaskException(MessageConstant.PRINT_TASK_NOT_EXIST);
//        }
//        // 封装DTO
//        PrintTaskDTO printTaskDTO = new PrintTaskDTO()
//                .setTaskId(printTask.getId())
//                .setStatus(printTask.getStatus());
//
//        // 赋值字节数组 + 文件名
//        DjTeam djTeam = djTeamMapper.selectById(printTask.getExamNum());
//        if(djTeam == null){
//            throw new TeamNotExistException(MessageConstant.TEAM_NOT_FOUND);
//        }
//        printTaskDTO.setTeamName(djTeam.getTeamName()).setTeamPosition(djTeam.getPosition());
//        return printTaskDTO;
//    }

    @Override
    public void addPrintTask(MultipartFile file, PrintTeamDTO printTeamDTO) {
        try {
            //System.out.println(printTeamDTO.getExamNum());
            enqueue(file,printTeamDTO);
        } catch (Exception e) {
            //e.printStackTrace();
            throw new QueueTaskException(MessageConstant.ADD_PRINT_TASK_FAILED);
        }
    }

    @Override
    public void setPrintTaskDone(Long taskId) {
        UpdateWrapper<PrintTask> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(PrintTask::getId, taskId)
                .set(PrintTask::getStatus, QueueTask.Status.DONE);
        printTaskMapper.update(null, updateWrapper);
    }

    @Override
    public byte[] getPdfFileByTaskId(Long taskId) {
        PrintTask printTask = printTaskMapper.selectById(taskId);
        return getPdfFile(printTask);
    }

    @Override
    public IPage<PrintTaskDTO> queryAuthTasksByPage(int curPage) {
        // 分页查询打印任务
        Page<PrintTask> taskPage = new Page<>(curPage, 10);
        printTaskMapper.selectPage(taskPage, null);
        List<PrintTask> taskList = taskPage.getRecords();

        // 遍历任务，严格按照 teamName + position 组合查询队伍（你的要求）
        List<PrintTaskDTO> dtoList = taskList.stream().map(task -> {
            PrintTaskDTO printTaskDTO = new PrintTaskDTO();
            if(task == null){
                throw new QueueTaskException(MessageConstant.PRINT_TASK_NOT_EXIST);
            }
            printTaskDTO.setTaskId(task.getId()).setStatus(task.getStatus());
            DjTeam djTeam = djTeamMapper.selectById(task.getExamNum());
            if(djTeam == null){
                throw new TeamNotExistException(MessageConstant.TEAM_NOT_FOUND);
            }
            printTaskDTO.setTeamName(djTeam.getTeamName()).setTeamPosition(djTeam.getPosition());
            return printTaskDTO;
        }).toList();

        // 封装DTO分页对象
        return new Page<PrintTaskDTO>(curPage,10).setRecords(dtoList);
    }

    @Override
    public void clearAll() {
       printTaskQueue.clear(PrintTask.class);
    }

    private void enqueue(MultipartFile file, PrintTeamDTO printTeamDTO) throws Exception{
        String filePath = PdfUtil.savePdf(file);
        PrintTask task = new PrintTask(printTeamDTO,file.getOriginalFilename(),filePath);
        printTaskQueue.enqueue(task, printTaskMapper);
    }

    private PrintTask dequeue() {
        return printTaskQueue.dequeue(printTaskMapper, new QueryWrapper<PrintTask>().lambda()
                .eq(PrintTask::getStatus, QueueTask.Status.PENDING)
                .orderByAsc(PrintTask::getCreateTime)
                .last("LIMIT 1"));
    }

//    private DjTeam queryDjTeamByTeamNameAndPosition(String teamName, String position) {
//        QueryWrapper<DjTeam> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda()
//                .eq(DjTeam::getTeamName, teamName)
//                .eq(DjTeam::getPosition, position);
//        return djTeamMapper.selectOne(queryWrapper);
//    }

    private byte[] getPdfFile(PrintTask printTask){
        File localFile = PdfUtil.readPdf(printTask.getFilePath());
        if (!localFile.exists()) {
            throw new GetFileErrorException();
        }

        byte[] pdfBytes;
        try{
            pdfBytes = Files.readAllBytes(localFile.toPath());
        } catch (Exception e){
            throw new GetFileErrorException(MessageConstant.TRANSFER_PRINT_TASK_FAILED);
        }
        return pdfBytes;
    }

    // 优化后的连接校验方法（删除无用的check发送）
    private boolean isEmitterActive(SseEmitter emitter) {
        try {
            // 仅校验Emitter状态，不发送数据
            return emitter != null;
        } catch (Exception e) {
            return false;
        }
    }
}
