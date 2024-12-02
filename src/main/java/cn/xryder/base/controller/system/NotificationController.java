package cn.xryder.base.controller.system;

import cn.xryder.base.config.OperationLog;
import cn.xryder.base.domain.PageResult;
import cn.xryder.base.domain.ResultJson;
import cn.xryder.base.domain.dto.system.NotificationDTO;
import cn.xryder.base.domain.vo.NotificationVO;
import cn.xryder.base.service.system.NotificationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/9/25 9:26
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @OperationLog("发送通知")
    @PostMapping
    @PreAuthorize("hasAuthority('system:notify:all')")
    public ResultJson<?> addPosition(@Valid @RequestBody NotificationDTO notificationDTO, Principal principal) {
        notificationService.send(notificationDTO, principal.getName());
        return ResultJson.ok();
    }

    @GetMapping("")
    @PreAuthorize("hasAuthority('system:notify:all')")
    public ResultJson<PageResult<List<NotificationVO>>> getNotifications(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            Principal principal
            ) {
        if (page <= 1) {
            page = 1;
        }
        return ResultJson.ok(notificationService.getNotifications(q, page, pageSize, principal.getName()));
    }
}
