package cn.xryder.base.ai.agent;
/*
 * MIT License
 *
 * Copyright (c) 2024 joetao
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

import cn.xryder.base.domain.vo.AiChatMonitorVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 监控分析AI智能体
 *
 * @Author: joetao
 * @Date: 2025/1/17 14:43
 */
@Component
@Slf4j
public class MonitorAgent {
    private final ChatClient chatClient;
    @PersistenceContext
    private EntityManager entityManager;

    public MonitorAgent(OpenAiChatModel chatModel) {
        String systemPrompt = """
                你是莱德队长，一个数据库专家
                                
                """;
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .build();
    }

    public String getDataSourceMeta() {
        return """
                数据库表元信息：
                表1名称:
                monitor_visitor
                                
                表1描述：
                monitor_visitor 是一个记录用户访问系统记录的表。
                                
                字段信息：
                id
                类型：BIGINT (bigint)
                属性：自增主键
                描述：唯一标识每条系统访问记录。
                                
                useruuid
                类型：VARCHAR
                属性：非空
                描述：用户标识，一般基于用户的浏览器生成唯一标识。
                                
                visit——date
                类型：DATETIME
                属性：非空
                描述：系统访问时间。
                表用途：
                记录独立访客，当天如果用户有访问系统会记录一条内容。
                      
                任务指引：
                如果需要根据上述表生成 SQL 查询，确保字段名和类型的正确性，返回字段是统计值的用value表示，放在返回字段最后。
                """;
    }

    public SystemMessage getSystemMessage() {
        String systemText = """
                你是一个专业的数据库管理员，非常擅长处理SQL。
                               
                SQL 语句应符合以下要求：
                                
                不使用任何 Markdown 或代码块标识（例如 sql）。
                只返回纯 SQL 语句，不附带额外的注释或说明。
                """;
        return new SystemMessage(systemText);
    }

    public Message getUserMessage(String question) {
        String context = getDataSourceMeta();
        String userText = """
                请生成标准的 SQL 查询语句。
                            
                {context}
                            
                问题：
                {question}
                """;
        PromptTemplate promptTemplate = new PromptTemplate(userText);
        return promptTemplate.createMessage(Map.of("context", context, "question", question));
    }

    public String generateSql(String question) {
        SystemMessage systemMessage = getSystemMessage();
        Message userMessage = getUserMessage(question);
        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));
        OpenAiChatOptions options = OpenAiChatOptions.builder().temperature(0.7).build();
        return chatClient.prompt(prompt).options(options).call().content();
    }

    public record Result(String sql, List<Map<String, Object>> data, List<String> fields) {
    }

    public String getTitle(String question) {
        String template = """
                请根据问题提炼中文标题。
                                
                以下是一个具体示例：
                问题：哪天的系统访客数最多？
                你的回答：系统访客最多的日期
                                
                问题如下:
                                
                {question}
                """;
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of("question", question));
        return chatClient.prompt()
                .user(prompt.getContents())
                .call()
                .content();
    }

    public String getSummary(String question, String data) {
        String template = """
                请根据问题和SQL查询结果对结果做一个概括，150字以内。
                                
                问题如下:
                                
                {question}
                                
                查询结果如下：
                                
                {data}
                """;
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of("question", question, "data", data));
        return chatClient.prompt()
                .user(prompt.getContents())
                .call().content();
    }

    public String getChartType(String question, String data) {
        String template = """
                请根据问题和数据推荐一个合适的用于展示该数据的图表, 只需要给出图表类型的英文名称。
                                
                问题如下:
                                
                {question}
                                
                数据如下：
                                
                {data}
                                
                可选择的图表类型如下：
                                
                Area, Line, Bar, Radar
                                
                以下是一个具体示例：
                问题：统计最近30天每天的网站访问量
                你的回答：Line
                """;
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Prompt prompt = promptTemplate.create(Map.of("question", question, "data", data));
        return chatClient.prompt()
                .user(prompt.getContents())
                .call().content();
    }

    public Result analyse(String question) {
        String originSql = generateSql(question);
        int retryCount = 2; // 最大重试次数
        List<Map<String, Object>> result = new ArrayList<>();

        for (int attempt = 0; attempt < retryCount; attempt++) {
            try {
                // 移除多余字符，清理 SQL
                String sql = cleanSql(originSql);
                List<String> fieldNames = getReturnFields(sql);
                Query query = entityManager.createNativeQuery(sql);
                log.info("执行sql: \n{}", sql);
                List<?> resultList = query.getResultList(); // 使用通配符适配多种类型的返回值

// 转换为 Map 列表
                for (Object row : resultList) {
                    Map<String, Object> map = new HashMap<>();
                    if (row instanceof Object[]) {
                        // 多列的情况
                        Object[] rowArray = (Object[]) row;
                        for (int i = 0; i < fieldNames.size(); i++) {
                            map.put(fieldNames.get(i), rowArray[i]);
                        }
                    } else {
                        // 单列的情况
                        if (fieldNames.size() != 1) {
                            throw new IllegalArgumentException("字段名数量与单列结果不匹配！");
                        }
                        map.put(fieldNames.get(0), row); // 单列情况下，直接存储结果
                    }
                    result.add(map);
                }
                return new Result(originSql, result, fieldNames); // 成功获取结果后直接返回
            } catch (Exception e) {
                log.error("SQL 执行失败: " + e.getMessage());

                // 如果是最后一次重试，抛出异常
                if (attempt == retryCount - 1) {
                    return new Result("SQL执行失败！", result, List.of()); // 成功获取结果后直接返回
                }

                // 尝试重新生成 SQL 和字段
                log.warn("尝试重新生成 SQL...");
                originSql = generateSqlWithErrorHandling(originSql, e.getMessage());
            }
        }
        return new Result("SQL执行失败！", result, List.of()); // 成功获取结果后直接返回
    }

    public List<String> getReturnFields(String sql) {
        // 初始化返回字段列表
        List<String> fieldNames = new ArrayList<>();

        // 使用 Hibernate Session 获取 Connection
        Session session = entityManager.unwrap(Session.class);

        session.doWork(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSetMetaData metaData = statement.getMetaData();
                if (metaData != null) {
                    int columnCount = metaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        fieldNames.add(metaData.getColumnName(i));
                    }
                }
            }
        });

        return fieldNames;
    }

    public AiChatMonitorVO notSupport() {
        AiChatMonitorVO aiChatMonitorVO = new AiChatMonitorVO();
        aiChatMonitorVO.setMessage("对不起，这已经超出了我的能力范围！请试试：最近7天，哪天的访问量最多？");
        return aiChatMonitorVO;
    }

    // 清理 SQL 的方法
    private String cleanSql(String sql) {
        return sql.replaceAll("```", "").replace("sql", "").trim();
    }

    // 调用大模型重新生成 SQL
    private String generateSqlWithErrorHandling(String originalSql, String errorMessage) {
        String question = "请根据以下 SQL 和错误信息修复 SQL：" + "\nSQL: " + originalSql + "\n错误信息: " + errorMessage + "\n注意：只需要返回新生成的SQL。";
        return generateSql(question);
    }
}
