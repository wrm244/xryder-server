package cn.xryder.base.controller.system;

import cn.xryder.base.domain.PageResult;
import cn.xryder.base.domain.R;
import cn.xryder.base.domain.vo.LoginLogVO;
import cn.xryder.base.domain.vo.OperationLogVO;
import cn.xryder.base.service.system.LogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/9/12 13:38
 */
@RestController
@RequestMapping("/api/v1/logs")
public class LogController {
    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/login")
    @PreAuthorize("hasAuthority('system')")
    public R<PageResult<List<LoginLogVO>>> getLoginLog(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        if (page <= 1) {
            page = 1;
        }
        return R.ok(logService.queryLoginLog(q, page, pageSize));
    }

    @GetMapping("/operation")
    @PreAuthorize("hasAuthority('system')")
    public R<PageResult<List<OperationLogVO>>> getOperationLog(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        if (page <= 1) {
            page = 1;
        }
        return R.ok(logService.queryOperationLog(q, page, pageSize));
    }
}
