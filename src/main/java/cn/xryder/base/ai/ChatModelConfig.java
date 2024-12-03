package cn.xryder.base.ai;

import cn.xryder.base.ai.functions.CurrentTaskFun;
import cn.xryder.base.ai.functions.GetDepartmentsFun;
import cn.xryder.base.ai.functions.GetNewMailFun;
import cn.xryder.base.ai.functions.SendMailFun;
import cn.xryder.base.repo.system.DepartmentRepo;
import cn.xryder.base.repo.system.NotificationRepo;
import cn.xryder.base.repo.system.UserNotificationRepo;
import cn.xryder.base.repo.system.UserRepo;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 大模型函数调用定义
 * @Author: joetao
 * @Date: 2024/10/10 9:10
 */
@Configuration
public class ChatModelConfig {
    private final NotificationRepo notificationRepo;
    private final UserNotificationRepo userNotificationRepo;
    private final UserRepo userRepo;
    private final DepartmentRepo departmentRepo;

    public ChatModelConfig(NotificationRepo notificationRepo, UserNotificationRepo userNotificationRepo, UserRepo userRepo, DepartmentRepo departmentRepo) {
        this.notificationRepo = notificationRepo;
        this.userNotificationRepo = userNotificationRepo;
        this.userRepo = userRepo;
        this.departmentRepo = departmentRepo;
    }

    @Bean
    public FunctionCallback currentTaskFun() {
        return FunctionCallback.builder()
                .function("currentTasks", new CurrentTaskFun())
                .description("get team's tasks ") // (2) 函数描述
                .inputType(CurrentTaskFun.Request.class)
                .build();
    }

    @Bean
    public FunctionCallback sendMailFun() {
        return FunctionCallback.builder()
                .function("sendMail", new SendMailFun(notificationRepo, userNotificationRepo, userRepo))
                .description("发送邮件给所有人。邮件包含的标题和内容需要用户进行输入。有数据库插入操作。") // (2) 函数描述
                .inputType(SendMailFun.Request.class)
                .build();
    }

    @Bean
    public FunctionCallback getNewMailFun() {
        return FunctionCallback.builder()
                .function("getNewMails", new GetNewMailFun(userNotificationRepo))
                .description("获取当前用户的未读邮件，用户可以选择查询多少封未读邮件，不选择则选择10封。") // (2) 函数描述
                .inputType(GetNewMailFun.Request.class)
                .build();
    }

    @Bean
    public FunctionCallback getDepartments() {
        return FunctionCallback.builder()
                .function("getDepartments", new GetDepartmentsFun(departmentRepo))
                .description("根据上级部门id获取子部门信息，如果上级部门未提及，则为顶级部门：公司，部门编号为1") // (2) 函数描述
                .inputType(GetDepartmentsFun.Request.class)
                .build();
    }
}
