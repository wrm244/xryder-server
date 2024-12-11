package cn.xryder.base.controller;

import cn.xryder.base.common.Admin;
import cn.xryder.base.config.OperationLog;
import cn.xryder.base.domain.ResultJson;
import cn.xryder.base.domain.dto.AccountDTO;
import cn.xryder.base.domain.dto.system.PasswordChangeDTO;
import cn.xryder.base.domain.entity.system.Avatar;
import cn.xryder.base.domain.vo.UserVO;
import cn.xryder.base.exception.custom.BadRequestException;
import cn.xryder.base.service.user.AccountService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

/**
 * @Author: joetao
 * @Date: 2024/8/12 14:13
 */
@RestController
@RequestMapping("/api/v1/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("")
    public ResultJson<UserVO> getAccount(Principal principal) {
        String name = principal.getName();
        UserVO accountInfo = accountService.getAccountInfo(name);
        return ResultJson.ok(accountInfo);
    }

    @OperationLog("修改账户信息")
    @PutMapping("")
    public ResultJson<UserVO> updateAccount(Principal principal, @RequestBody AccountDTO account) {
        String username = principal.getName();
        UserVO accountInfo = accountService.updateAccount(username, account);
        return ResultJson.ok(accountInfo);
    }

    @OperationLog("修改账户密码")
    @PutMapping("/password")
    public ResultJson changePassword(Principal principal, @RequestBody PasswordChangeDTO passwordChange) throws Exception {
        String username = principal.getName();
        if (Admin.username.equals(username)) {
            throw new BadRequestException("不能修改超管账号！");
        }
        String oldPassword = passwordChange.getOldPassword();
        String newPassword = passwordChange.getNewPassword();
        accountService.changePassword(username, oldPassword, newPassword);
        return ResultJson.ok();
    }

    @OperationLog("更新头像")
    @PostMapping("/avatar")
    public ResultJson<Avatar> uploadAvatarImage(@RequestParam("file")MultipartFile file, Principal principal) throws IOException {
        return ResultJson.ok(accountService.saveAvatar(file, principal.getName()));
    }
}

