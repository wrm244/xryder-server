package cn.xryder.base.service.user;

import cn.xryder.base.domain.vo.MailVO;

import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/9/26 15:24
 */
public interface MailService {
    /**
     * 获取邮件
     *
     * @param status 其他：全部邮件 0：未读邮件
     * @return
     */
    List<MailVO> getMails(String username, Integer status);

    /**
     * 邮件已读
     *
     * @param id   用户通知id
     * @param name 用户账户
     */
    void read(String name, Long id);

    /**
     * 删除邮件
     *
     * @param name 用户账户
     * @param id   用户通知id
     */
    void delete(String name, Long id);
}
