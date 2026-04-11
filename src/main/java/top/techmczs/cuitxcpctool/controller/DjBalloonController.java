package top.techmczs.cuitxcpctool.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.techmczs.cuitxcpctool.constant.ResponseMessageConstant;
import top.techmczs.cuitxcpctool.entity.Balloon;
import top.techmczs.cuitxcpctool.result.Result;
import top.techmczs.cuitxcpctool.services.DjBalloonService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/balloon")
public class DjBalloonController {

    private final DjBalloonService djBalloonService;

    @GetMapping("/task/page")
    public Result<IPage<Balloon>> getAllPrintTask(@RequestParam(value = "cur_page") int curPage){
        return Result.success(djBalloonService.getAllBalloonFromDomjudge(curPage));
    }

    @PostMapping("/task/{id}/done")
    public Result<String> doneBalloon(@PathVariable Long id) {
        djBalloonService.setBalloonDone(id);
        return Result.success(ResponseMessageConstant.SUCCESS);
    }
}