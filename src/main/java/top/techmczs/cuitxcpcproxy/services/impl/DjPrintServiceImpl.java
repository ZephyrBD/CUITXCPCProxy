package top.techmczs.cuitxcpcproxy.services.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.techmczs.cuitxcpcproxy.constant.MessageConstant;
import top.techmczs.cuitxcpcproxy.dto.PrintTaskDTO;
import top.techmczs.cuitxcpcproxy.dto.PrintTeamDTO;
import top.techmczs.cuitxcpcproxy.entity.DjTeam;
import top.techmczs.cuitxcpcproxy.entity.queuetask.PrintTask;
import top.techmczs.cuitxcpcproxy.entity.queuetask.QueueTask;
import top.techmczs.cuitxcpcproxy.exception.GetFileErrorException;
import top.techmczs.cuitxcpcproxy.exception.QueueTaskException;
import top.techmczs.cuitxcpcproxy.exception.TeamNotExistException;
import top.techmczs.cuitxcpcproxy.mapper.DjTeamMapper;
import top.techmczs.cuitxcpcproxy.mapper.PrintTaskMapper;
import top.techmczs.cuitxcpcproxy.result.Result;
import top.techmczs.cuitxcpcproxy.services.DjPrintService;
import top.techmczs.cuitxcpcproxy.utils.PdfUtil;
import top.techmczs.cuitxcpcproxy.utils.SqlQueue;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
@RequiredArgsConstructor
@Slf4j
public class DjPrintServiceImpl implements DjPrintService {

    private final SqlQueue<PrintTask> printTaskQueue;
    private final PrintTaskMapper printTaskMapper;
    private final DjTeamMapper djTeamMapper;

    // ✅ 修复1：线程安全的客户端列表（支持多管理员）
    private final Set<SseEmitter> PRINT_CLIENTS = new CopyOnWriteArraySet<>();

    // ===================== ✅ 修复2：注册SSE客户端 =====================
    @Override
    public void registerPrintClient(SseEmitter emitter) {
        PRINT_CLIENTS.add(emitter);

        // 生命周期管理（自动清理断开的客户端）
        emitter.onCompletion(() -> {
            PRINT_CLIENTS.remove(emitter);
            log.info("打印SSE连接关闭，剩余客户端：{}", PRINT_CLIENTS.size());
        });
        emitter.onTimeout(() -> {
            PRINT_CLIENTS.remove(emitter);
            emitter.complete();
        });
        emitter.onError(ex -> {
            PRINT_CLIENTS.remove(emitter);
            emitter.completeWithError(ex);
        });

        // 首次连接发送成功消息
        try {
            emitter.send(SseEmitter.event().name("connect").data(Result.success("打印SSE连接成功")));
        } catch (Exception e) {
            PRINT_CLIENTS.remove(emitter);
        }
    }

    // ===================== ✅ 修复3：定时推送打印任务（替代死循环） =====================
    @Scheduled(fixedRate = 1000)
    public void pushPrintTaskToClients() {
        // 1. 查询【待处理】打印任务（只查不消费！）
        List<PrintTask> pendingTasks = getPendingPrintTasks();
        if (pendingTasks.isEmpty()) {
            sendHeartbeat();
            return;
        }

        // 2. 组装任务DTO
        List<PrintTaskDTO> taskDTOList = buildPrintTaskDTO(pendingTasks);

        // 3. 广播给所有管理员客户端
        broadcastPrintTask(taskDTOList);
    }

    // ===================== 工具：查询待处理任务 =====================
    private List<PrintTask> getPendingPrintTasks() {
        QueryWrapper<PrintTask> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(PrintTask::getStatus, QueueTask.Status.PENDING)
                .orderByAsc(PrintTask::getCreateTime);
        return printTaskMapper.selectList(wrapper);
    }

    // ===================== 工具：组装打印任务DTO =====================
    private List<PrintTaskDTO> buildPrintTaskDTO(List<PrintTask> tasks) {
        if (tasks.isEmpty()) return Collections.emptyList();

        return tasks.stream().map(task -> {
            DjTeam djTeam = djTeamMapper.selectById(task.getExamNum());
            String teamName = djTeam == null ? "未知队伍" : djTeam.getTeamName();
            String position = djTeam == null ? "未知位置" : djTeam.getPosition();

            return new PrintTaskDTO()
                    .setTaskId(task.getId())
                    .setTeamName(teamName)
                    .setTeamPosition(position)
                    .setStatus(QueueTask.Status.PENDING);
        }).toList();
    }

    // ===================== 工具：广播消息给所有客户端 =====================
    private void broadcastPrintTask(List<PrintTaskDTO> dtoList) {
        Iterator<SseEmitter> iterator = PRINT_CLIENTS.iterator();
        while (iterator.hasNext()) {
            SseEmitter emitter = iterator.next();
            try {
                emitter.send(SseEmitter.event()
                        .name("printTask")
                        .data(Result.success(dtoList)));
            } catch (Exception e) {
                iterator.remove();
                log.error("打印任务推送失败，客户端已断开");
            }
        }
    }

    // ===================== 工具：有效心跳保活 =====================
    private void sendHeartbeat() {
        Iterator<SseEmitter> iterator = PRINT_CLIENTS.iterator();
        while (iterator.hasNext()) {
            SseEmitter emitter = iterator.next();
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
            } catch (Exception e) {
                iterator.remove();
            }
        }
    }

    // ===================== 以下为原有业务逻辑（完全不变）=====================
    @Override
    public void addPrintTask(MultipartFile file, PrintTeamDTO printTeamDTO) {
        try {
            enqueue(file, printTeamDTO);
        } catch (Exception e) {
            throw new QueueTaskException(MessageConstant.ADD_PRINT_TASK_FAILED);
        }
    }

    @Override
    public void setPrintTaskDone(Long taskId) {
        printTaskMapper.update(null, new UpdateWrapper<PrintTask>().lambda()
                .eq(PrintTask::getId, taskId)
                .set(PrintTask::getStatus, QueueTask.Status.DONE));
    }

    @Override
    public byte[] getPdfFileByTaskId(Long taskId) {
        PrintTask printTask = printTaskMapper.selectById(taskId);
        return getPdfFile(printTask);
    }

    @Override
    public IPage<PrintTaskDTO> queryAuthTasksByPage(int curPage) {
        Page<PrintTask> taskPage = new Page<>(curPage, 10);
        printTaskMapper.selectPage(taskPage, null);

        List<PrintTaskDTO> dtoList = taskPage.getRecords().stream().map(task -> {
            DjTeam djTeam = djTeamMapper.selectById(task.getExamNum());
            if (djTeam == null) throw new TeamNotExistException(MessageConstant.TEAM_NOT_FOUND);
            return new PrintTaskDTO()
                    .setTaskId(task.getId())
                    .setTeamName(djTeam.getTeamName())
                    .setTeamPosition(djTeam.getPosition())
                    .setStatus(task.getStatus());
        }).toList();

        return new Page<PrintTaskDTO>(curPage, 10).setRecords(dtoList);
    }

    @Override
    public void clearAll() {
        printTaskQueue.clear(PrintTask.class);
    }

    private void enqueue(MultipartFile file, PrintTeamDTO printTeamDTO) throws Exception {
        String filePath = PdfUtil.savePdf(file);
        PrintTask task = new PrintTask(printTeamDTO, file.getOriginalFilename(), filePath);
        printTaskQueue.enqueue(task, printTaskMapper);
    }

    private byte[] getPdfFile(PrintTask printTask) {
        File localFile = PdfUtil.readPdf(printTask.getFilePath());
        if (!localFile.exists()) {
            throw new GetFileErrorException();
        }
        try {
            return Files.readAllBytes(localFile.toPath());
        } catch (Exception e) {
            throw new GetFileErrorException(MessageConstant.TRANSFER_PRINT_TASK_FAILED);
        }
    }
}