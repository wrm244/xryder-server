package cn.xryder.base.service.user;

import cn.xryder.base.domain.entity.system.UserNotification;
import cn.xryder.base.domain.vo.MailVO;
import cn.xryder.base.repo.system.NotificationRepo;
import cn.xryder.base.repo.system.UserNotificationRepo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/9/26 15:27
 */
@Service
public class MailServiceImpl implements MailService{
    private final UserNotificationRepo userNotificationRepo;
    private final int READ = 1;

    public MailServiceImpl(UserNotificationRepo userNotificationRepo) {
        this.userNotificationRepo = userNotificationRepo;
    }

    @Override
    public List<MailVO> getMails(String username, Integer status) {
        Pageable limit = PageRequest.of(0, 100);  // 限制100条
        List<UserNotification> notifications;
        int unread = 0;
        if (status != null && unread == status) {
            notifications = userNotificationRepo.findTop100UnreadByUsername(username, limit);
        } else {
            notifications = userNotificationRepo.findTop100ByUsername(username, limit);
        }
        List<MailVO> mails = new ArrayList<>();
        for (UserNotification userNotification: notifications) {
            mails.add(MailVO.builder()
                            .id(userNotification.getNotification().getId())
                            .title(userNotification.getNotification().getTitle())
                            .content(userNotification.getNotification().getContent())
                            .createTime(userNotification.getNotification().getCreateTime())
                            .hasRead(userNotification.getStatus() == READ)
                    .build());
        }
        return mails;
    }

    @Override
    public void read(String name, Long id) {
        UserNotification userNotification = userNotificationRepo.findUserNotificationByNotificationIdAndUsername(id, name);
        if (userNotification != null) {
            userNotification.setStatus(READ);
            userNotificationRepo.save(userNotification);
        }
    }

    @Override
    public void delete(String name, Long id) {
        UserNotification userNotification = userNotificationRepo.findUserNotificationByNotificationIdAndUsername(id, name);
        if (userNotification != null) {
            userNotificationRepo.delete(userNotification);
        }
    }
}
