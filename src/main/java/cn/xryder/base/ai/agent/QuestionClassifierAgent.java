package cn.xryder.base.ai.agent;
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

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author: joetao
 * @Date: 2025/1/17 14:54
 */
@Component
public class QuestionClassifierAgent {
    private final OpenAiChatModel chatModel;

    public QuestionClassifierAgent(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    record QuestionType(Integer type, String description) {
    }

    //问题分类
    public Integer getQuestionType(String question) {
        String template = """
                请将问题按以下类别进行分类。
                1: 数据分析类问题
                2: 其他类型问题
                问题如下：
                {question}
                
                以下是具体交互示例：
                - **问答示例**：
                用户：统计最近14天每天的访客数是多少？
                你的回答：1
                
                用户：哪天的访客数最多？
                你的回答：1
                
                用户：你是谁？
                你的回答：2
                """;
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of("question", question));
        return ChatClient.builder(chatModel)
                .defaultSystem("你非常擅长对用户问题进行分类，可以将问题打上类别标签")
                .build()
                .prompt()
                .user(prompt.getContents())
                .call()
                .entity(QuestionType.class).type;
    }
}
