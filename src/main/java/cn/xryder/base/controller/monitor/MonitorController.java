package cn.xryder.base.controller.monitor;
/*
 * MIT License
 *
 * Copyright (c) 2024 joetao
 *
 * Contact me via email: joetao8877@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import cn.xryder.base.ai.agent.MonitorAgent;
import cn.xryder.base.ai.agent.QuestionClassifierAgent;
import cn.xryder.base.config.OperationLog;
import cn.xryder.base.domain.R;
import cn.xryder.base.domain.vo.AiChatMonitorVO;
import cn.xryder.base.exception.custom.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: joetao
 * @Date: 2025/1/17 14:58
 */
@RestController
@RequestMapping("/api/v1/monitor")
public class MonitorController {
    private final ObjectMapper objectMapper;
    private final MonitorAgent monitorAgent;
    private final QuestionClassifierAgent questionClassifierAgent;

    public MonitorController(ObjectMapper objectMapper, MonitorAgent monitorAgent, QuestionClassifierAgent questionClassifierAgent) {
        this.objectMapper = objectMapper;
        this.monitorAgent = monitorAgent;
        this.questionClassifierAgent = questionClassifierAgent;
    }

    @OperationLog("智能监控问答")
    @GetMapping("/chat")
    public R<AiChatMonitorVO> chat(@RequestParam(value = "question") String question) throws JsonProcessingException {
        Integer questionType = questionClassifierAgent.getQuestionType(question);
        if (questionType == 1) {
            return R.ok(handleDataAnalyse(question));
        }

        return R.ok(monitorAgent.notSupport());
    }

    private AiChatMonitorVO handleDataAnalyse(String message) throws JsonProcessingException {
        MonitorAgent.Result result = monitorAgent.analyse(message);

        if (result.data().size() == 0) {
            throw new BadRequestException("未检索到数据！");
        }
        String title = monitorAgent.getTitle(message);
        String dataJson = objectMapper.writeValueAsString(result.data());
        String summary = monitorAgent.getSummary(message, dataJson);
        String chartType = monitorAgent.getChartType(message, dataJson);

        return new AiChatMonitorVO(result.data(), title, summary, chartType, result.sql(), result.fields(), "");
    }
}
