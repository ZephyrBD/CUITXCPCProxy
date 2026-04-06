package top.techmczs.cuitxcpcproxy.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.techmczs.cuitxcpcproxy.entity.DjTeam;
import top.techmczs.cuitxcpcproxy.result.Result;
import top.techmczs.cuitxcpcproxy.services.DjTeamService;

import java.util.Objects;

@RestController()
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
public class DjTeamAdminController {

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
                return Result.error("必须是xlsx文件。");
            }
            djTeamService.importTeamExcel(file);
            return Result.success("队伍信息导入成功");
        } catch (Exception e){
            return Result.error("导入失败：" + e.getMessage());
        }
    }
}
