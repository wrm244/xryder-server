package cn.xryder.base.ai.functions;

import cn.xryder.base.domain.entity.system.UserNotification;
import cn.xryder.base.domain.vo.MailVO;
import cn.xryder.base.repo.system.UserNotificationRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * 获取新邮件函数
 * @Author: joetao
 * @Date: 2024/10/10 14:23
 */
@Slf4j
public class GetNewMailFun implements BiFunction<GetNewMailFun.Request, ToolContext , GetNewMailFun.Response> {
    private final UserNotificationRepo userNotificationRepo;

    public GetNewMailFun(UserNotificationRepo userNotificationRepo) {
        this.userNotificationRepo = userNotificationRepo;
    }
    // 必须有参数
    public record Request(Integer number) {
    }

    public record Response(List<MailVO> mails) {

    }

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        Integer requestNumber = request.number;
        int number = (requestNumber != null) ? requestNumber : 100;

        Pageable limit = PageRequest.of(0, number);
        String username = (String) toolContext.getContext().get("username");
        List<UserNotification> notifications = userNotificationRepo.findTop100UnreadByUsername(username, limit);
        List<MailVO> mails = new ArrayList<>();
        for (UserNotification userNotification: notifications) {
            mails.add(MailVO.builder()
                    .id(userNotification.getNotification().getId())
                    .title(userNotification.getNotification().getTitle())
                    .content(userNotification.getNotification().getContent())
                    .sendTime(userNotification.getNotification().getCreateTime())
                    .build());
        }
        return new Response(mails);
    }
}
