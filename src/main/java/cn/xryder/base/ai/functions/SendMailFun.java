package cn.xryder.base.ai.functions;

import cn.xryder.base.domain.entity.system.Notification;
import cn.xryder.base.domain.entity.system.User;
import cn.xryder.base.domain.entity.system.UserNotification;
import cn.xryder.base.repo.system.NotificationRepo;
import cn.xryder.base.repo.system.UserNotificationRepo;
import cn.xryder.base.repo.system.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * 邮件发送函数
 *
 * @Author: joetao
 * @Date: 2024/10/10 14:23
 */
@Slf4j
public class SendMailFun implements BiFunction<SendMailFun.Request, ToolContext, SendMailFun.Response> {
    private final NotificationRepo notificationRepo;
    private final UserNotificationRepo userNotificationRepo;
    private final UserRepo userRepo;

    public SendMailFun(NotificationRepo notificationRepo, UserNotificationRepo userNotificationRepo, UserRepo userRepo) {
        this.notificationRepo = notificationRepo;
        this.userNotificationRepo = userNotificationRepo;
        this.userRepo = userRepo;
    }

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        String content = request.content;
        String title = request.title;
        Notification notification = new Notification();
        String username = (String) toolContext.getContext().get("username");
        notification.setCreator(username);
        notification.setCreateTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());
        notification.setContent(content);
        notification.setTitle(title);
        notification.setType(1);
        notificationRepo.save(notification);
        int unread = 0;
        Set<String> usernames = userRepo.findAll().stream().map(User::getUsername).collect(Collectors.toSet());
        List<UserNotification> userNotifications = new ArrayList<>();
        usernames.forEach(u -> {
            UserNotification userNotification = new UserNotification();
            userNotification.setNotification(notification);
            userNotification.setUsername(u);
            userNotification.setStatus(unread);
            userNotifications.add(userNotification);
        });
        userNotificationRepo.saveAll(userNotifications);
        return new Response("邮件已成功发送给" + userNotifications.size() + "人！");
    }

    public record Request(String title, String content) {
    }

    public record Response(String result) {
    }
}
