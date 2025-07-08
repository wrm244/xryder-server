package cn.xryder.base.service.system;

import cn.xryder.base.domain.PageResult;
import cn.xryder.base.domain.dto.system.NotificationDTO;
import cn.xryder.base.domain.vo.NotificationVO;

import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/9/25 9:44
 */
public interface NotificationService {
    void send(NotificationDTO notificationDTO, String username);

    /**
     * 查询已发送的通知
     *
     * @param q        查询条件
     * @param page     第几页
     * @param pageSize 每页行数
     * @param username 发送人账户
     * @return 分页数据
     */
    PageResult<List<NotificationVO>> getNotifications(String q, int page, int pageSize, String username);
}
