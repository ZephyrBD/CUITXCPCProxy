package top.techmczs.cuitxcpctool.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.techmczs.cuitxcpctool.constant.MessageConstant;
import top.techmczs.cuitxcpctool.constant.ResponseMessageConstant;
import top.techmczs.cuitxcpctool.entity.DjTeam;
import top.techmczs.cuitxcpctool.result.Result;
import top.techmczs.cuitxcpctool.services.DjTeamService;

import java.util.Objects;

@RestController()
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
public class DjTeamController {

    private final DjTeamService djTeamService;

    @GetMapping("/team/page")
    public Result<IPage<DjTeam>> getAllTeamsByPage(@RequestParam(value = "cur_page") int curPage){
        return Result.success(djTeamService.queryTeamsByPage(curPage));
    }

    /**
     * 清空队伍数据库，并导入队伍数据
     * @param file 队伍名单
     * @return 导入状态
     */
    @PostMapping("/team")
    public Result<String> uploadTeam(@RequestPart MultipartFile file){
        try {
            if(!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xlsx")){
                return Result.error(MessageConstant.MUST_XLSX);
            }
            djTeamService.importTeamExcel(file);
            return Result.success(ResponseMessageConstant.SUCCESS);
        } catch (Exception e){
            e.printStackTrace();
            return Result.error(ResponseMessageConstant.FAILED);
        }
    }
}
