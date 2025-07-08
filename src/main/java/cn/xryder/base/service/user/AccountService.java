package cn.xryder.base.service.user;

import cn.xryder.base.domain.dto.AccountDTO;
import cn.xryder.base.domain.entity.system.Avatar;
import cn.xryder.base.domain.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Author: joetao
 * @Date: 2024/8/20 10:50
 */
public interface AccountService {
    /**
     * 获取登录账户信息
     *
     * @param username 用户名
     * @return 账户用户信息
     */
    UserVO getAccountInfo(String username);

    /**
     * 上传头像
     *
     * @param file     头像文件
     * @param username 用户名
     * @return 头像信息
     * @throws IOException 异常
     */
    Avatar saveAvatar(MultipartFile file, String username) throws IOException;

    /**
     * 更新账户信息
     *
     * @param username 用户名
     * @param account  更新信息
     * @return 账户信息
     */
    UserVO updateAccount(String username, AccountDTO account);

    /**
     * 修改密码
     *
     * @param username    用户名
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    void changePassword(String username, String oldPassword, String newPassword) throws Exception;
}
