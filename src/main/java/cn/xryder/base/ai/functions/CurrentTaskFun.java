package cn.xryder.base.ai.functions;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 模拟任务
 *
 * @Author: joetao
 * @Date: 2024/10/10 14:23
 */
@Slf4j
public class CurrentTaskFun implements Function<CurrentTaskFun.Request, CurrentTaskFun.Response> {

    @Override
    public Response apply(Request request) {
        List<String> tasks = new ArrayList<>();
        tasks.add("1. 开发任务调度功能");
        tasks.add("2. 重构数据治理系统");
        tasks.add("3. 修复数据开发任务的bug");
        return new Response(tasks);
    }

    public record Request(String team) {
    }

    public record Response(List<String> tasks) {
    }
}
