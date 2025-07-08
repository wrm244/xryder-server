package cn.xryder.base.controller.system;

import cn.xryder.base.config.OperationLog;
import cn.xryder.base.domain.R;
import cn.xryder.base.domain.vo.MailVO;
import cn.xryder.base.service.user.MailService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/9/26 15:20
 */
@RestController
@RequestMapping("/api/v1/mails")
public class MailController {
    private final MailService mailService;

    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    @GetMapping("")
    public R<List<MailVO>> getMails(@RequestParam(required = false) Integer status, Principal principal) {
        return R.ok(mailService.getMails(principal.getName(), status));
    }

    @OperationLog("阅读邮件")
    @PutMapping("/{id}/read")
    public R<?> readMails(@PathVariable Long id, Principal principal) {
        mailService.read(principal.getName(), id);
        return R.ok();
    }

    @OperationLog("删除邮件")
    @DeleteMapping("/{id}")
    public R<?> deleteMails(@PathVariable Long id, Principal principal) {
        mailService.delete(principal.getName(), id);
        return R.ok();
    }
}
