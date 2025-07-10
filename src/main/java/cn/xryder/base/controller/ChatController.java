package cn.xryder.base.controller;

import cn.xryder.base.common.FileReader;
import cn.xryder.base.config.OperationLog;
import cn.xryder.base.domain.R;
import cn.xryder.base.exception.custom.BadRequestException;
import cn.xryder.base.exception.custom.ServerException;
import cn.xryder.base.service.JwtService;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: joetao
 * @Date: 2024/9/30 9:29
 */
@RestController
@RequestMapping("/api/v1/ai")
@Slf4j
public class ChatController {
    private final ChatClient chatClient;
    private final FileReader fileReader;
    private final ConcurrentHashMap<String, HashMap<String, String>> conversationFileMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    public ChatController(@Qualifier("dashscopeChatModel") ChatModel chatModel, FileReader fileReader, ObjectMapper objectMapper,
                          JwtService jwtService) {
        String systemPrompt = """
                你是一个非常有帮助的智能助手.
                注意：
                1. 所有邮件都必须有标题和内容，而且标题和内容都是用户指定的，不能自行生成。
                2. 在发送任何一封邮件前，都需要跟用户确认发送的内容和发送的对象，只有用户同意了才能执行发送指令。
                """;
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultOptions(
                        DashScopeChatOptions.builder()
                                .withTopP(0.7)
                                .build()
                )
                .build();
        this.fileReader = fileReader;
        this.objectMapper = objectMapper;
        this.jwtService = jwtService;
    }

    @GetMapping("/token")
    public R<String> generate(Principal principal) {
        String aiChatToken = jwtService.generateAiChatToken(principal.getName());
        return R.ok(aiChatToken);
    }

    @OperationLog("发起对话")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String message,
                               @RequestParam String conversationId,
                               @RequestParam(required = false) String files,
                               @RequestHeader("Authorization") String authHeader) throws JsonProcessingException {

        log.info("开始处理对话请求 - conversationId: {}, hasFiles: {}", conversationId, files != null && !files.isEmpty());
        long startTime = System.currentTimeMillis();

        // 验证用户身份
        String username = "";
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            username = jwtService.extractUsername(token);
            log.debug("用户身份验证成功: {}", username);
            boolean isExpired = jwtService.isTokenExpired(token);
            if (isExpired) {
                log.warn("用户Token已过期: {}", token);
                return Flux.just("code: 405\n\n");
            }

        } else {
            log.warn("无效的授权header");
            return Flux.just("data: 无效的授权header\n\n");
        }

        // 构建提示词
        String prompt;
        try {
            if (files != null && !files.isEmpty()) {
                log.debug("处理文件上下文");
                String fileContext = getFiles(files, conversationId);
                prompt = getFileBasePrompt(fileContext, message);
            } else {
                prompt = message;
            }
        } catch (Exception e) {
            log.error("处理文件上下文时发生错误: {}", e.getMessage(), e);
            return Flux.just("data: 处理文件时发生错误，请重试！\n\n");
        }

        log.info("开始调用AI模型 - 耗时: {}ms", System.currentTimeMillis() - startTime);

        // 构建聊天流
        return chatClient.prompt()
                .advisors(advisor -> advisor.param("chat_memory_conversation_id", conversationId).param("chat_memory_response_size", 100))
                .user(prompt)
                .stream()
                .content()
                .filter(Objects::nonNull)
                .filter(content -> !content.trim().isEmpty())
                .doOnNext(data -> log.trace("发送数据块: {}", data.substring(0, Math.min(50, data.length()))))
                .doOnComplete(() -> {
                    long totalTime = System.currentTimeMillis() - startTime;
                    log.info("对话处理完成 - conversationId: {}, 总耗时: {}ms", conversationId, totalTime);
                })
                .doOnError(error -> {
                    long totalTime = System.currentTimeMillis() - startTime;
                    log.error("对话处理出错 - conversationId: {}, 耗时: {}ms, 错误: {}", conversationId, totalTime, error.getMessage(), error);
                })
                .onErrorResume(error -> Flux.just("data: 系统繁忙，请稍后重试！\n\n"));
    }

    private String getFileBasePrompt(String files, String question) {

        String userText = """
                请根据以下文件内容回答问题：
                {files}
                问题如下：
                {question}
                """;
        PromptTemplate promptTemplate = new PromptTemplate(userText);
        Prompt prompt = promptTemplate.create(Map.of("files", files, "question", question));
        return prompt.getContents();
    }

    private String getFiles(String files, String conversationId) throws JsonProcessingException {
        log.debug("开始处理文件上下文 - conversationId: {}", conversationId);

        HashMap<String, String> fileHashMap = conversationFileMap.get(conversationId);
        if (fileHashMap == null) {
            log.warn("未找到conversationId对应的文件: {}", conversationId);
            return "没有找到文件！";
        }

        List<String> fileNames;
        try {
            fileNames = objectMapper.readValue(files, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("解析文件名列表失败: {}", e.getMessage());
            throw e;
        }

        StringBuilder content = new StringBuilder();
        int processedFiles = 0;

        for (String fileName : fileNames) {
            String fileContent = fileHashMap.get(fileName);
            if (fileContent != null) {
                content.append("文件名：").append(fileName).append("\n");
                content.append(fileContent).append("\n\n");
                processedFiles++;
            } else {
                log.warn("文件不存在: {}", fileName);
            }
        }

        log.debug("文件处理完成 - 请求文件数: {}, 实际处理: {}", fileNames.size(), processedFiles);

        // 获取上传的文章后，清空该对话的文章缓存，该对话的缓存文章将存入对话记忆列表中。
        conversationFileMap.remove(conversationId);
        return content.toString();
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("conversationId") String conversationId) {
        log.info("开始处理文件上传 - conversationId: {}, 文件名: {}, 大小: {} bytes",
                conversationId, file.getOriginalFilename(), file.getSize());
        long startTime = System.currentTimeMillis();

        if (file.isEmpty()) {
            throw new BadRequestException("文件为空！");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BadRequestException("不支持的文件类型");
        }

        String originalFilename = file.getOriginalFilename();
        String content;

        try {
            switch (contentType) {
                case "text/plain" -> content = fileReader.readTxtFile(file);
                case "text/csv" -> content = fileReader.readCsvFile(file);
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ->
                        content = fileReader.readExcelFile(file);
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                        content = fileReader.readDocxFile(file);
                case "application/pdf" -> content = fileReader.readPdfFile(file);
                default -> throw new BadRequestException("不支持的文件类型！");
            }
        } catch (IOException | CsvException e) {
            log.error("文件读取失败 - 文件名: {}, 错误: {}", originalFilename, e.getMessage(), e);
            throw new ServerException("服务器错误！", e);
        }

        conversationFileMap.putIfAbsent(conversationId, HashMap.newHashMap(1));
        conversationFileMap.get(conversationId).put(originalFilename, content);

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("文件上传处理完成 - 文件名: {}, 内容长度: {} 字符, 耗时: {}ms",
                originalFilename, content.length(), processingTime);

        return originalFilename;
    }

    // 每天凌晨 1 点执行
    @Scheduled(cron = "0 0 1 * * ?")
    public void clearMap() {
        conversationFileMap.clear();
        log.info("Cleared conversation file map at scheduled time");
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("conversationCount", conversationFileMap.size());
        status.put("timestamp", System.currentTimeMillis());
        status.put("memoryUsage", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        status.put("totalMemory", Runtime.getRuntime().totalMemory());
        return status;
    }
}
