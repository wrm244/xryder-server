package cn.xryder.base.controller;

import cn.xryder.base.common.FileReader;
import cn.xryder.base.config.OperationLog;
import cn.xryder.base.domain.ResultJson;
import cn.xryder.base.exception.custom.BadRequestException;
import cn.xryder.base.exception.custom.ServerException;
import cn.xryder.base.service.JwtService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.exceptions.CsvException;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
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
    private final OpenAiChatModel chatModel;
    private final ChatClient chatClient;
    private final FileReader fileReader;
    private final ConcurrentHashMap<String, HashMap<String, String>> conversationFileMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    public ChatController(OpenAiChatModel chatModel, FileReader fileReader, ObjectMapper objectMapper, JwtService jwtService) {
        this.chatModel = chatModel;
        String systemPrompt = """
                你是一个非常有帮助的智能助手.
                注意：
                1. 所有邮件都必须有标题和内容，而且标题和内容都是用户指定的，不能自行生成。
                2. 在发送任何一封邮件前，都需要跟用户确认发送的内容和发送的对象，只有用户同意了才能执行发送指令。
                """;
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
        this.fileReader = fileReader;
        this.objectMapper = objectMapper;
        this.jwtService = jwtService;
    }

    @GetMapping("/token")
    public ResultJson<String> generate(Principal principal) {
        String aiChatToken = jwtService.GenerateAiChatToken(principal.getName());
        return ResultJson.ok(aiChatToken);
    }

    @GetMapping("/generate")
    public Map<String, String> generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", chatModel.call(message));
    }

    @OperationLog("发起对话")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String message,
                               @RequestParam String conversationId,
                               @RequestParam(required = false) String files,
                               @RequestHeader("Authorization") String authHeader) throws JsonProcessingException {

        String username = "";
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                username = jwtService.extractUsername(token);
            } catch (ExpiredJwtException e) {
                return Flux.just("登录已过期！");
            }
        } else {
            return Flux.just("登录已过期，请刷新当前页面！");
        }
        String prompt;
        if (files.length() > 0) {
            String fileContext = getFiles(files, conversationId);
            prompt = getFileBasePrompt(fileContext, message);
        } else {
            prompt = message;
        }
        return chatClient.prompt()
                .toolContext(Map.of("username", username))
                .advisors(advisor -> advisor.param("chat_memory_conversation_id", conversationId)
                        .param("chat_memory_response_size", 100))
                .user(prompt)
                .stream()
                .content()
                .filter(Objects::nonNull)
                .map(content -> "'" + content); // 添加结束标识符
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

        HashMap<String, String> fileHashMap = conversationFileMap.get(conversationId);
        if (fileHashMap == null) {
            return "没有找到文件！";
        }
        List<String> fileNames = objectMapper.readValue(files, List.class);
        StringBuilder content = new StringBuilder();
        for (String fileName : fileNames) {
            content.append("文件名：").append(fileName).append("\n");
            content.append(fileHashMap.get(fileName)).append("\n");
        }
        //获取上传的文章后，清空该对话的文章缓存，该对话的缓存文章将存入对话记忆列表中。
        conversationFileMap.remove(conversationId);
        return content.toString();
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("conversationId") String conversationId) {
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
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> content = fileReader.readExcelFile(file);
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> content = fileReader.readDocxFile(file);
                case "application/pdf" -> content = fileReader.readPdfFile(file);
                default -> throw new BadRequestException("不支持的文件类型！");
            }
        } catch (IOException | CsvException e) {
            throw new ServerException("服务器错误！", e);
        }

        conversationFileMap.putIfAbsent(conversationId, HashMap.newHashMap(1));
        conversationFileMap.get(conversationId).put(originalFilename, content);
        return originalFilename;
    }

    // 每天凌晨 1 点执行
    @Scheduled(cron = "0 0 1 * * ?")
    private void clearMap() {
        conversationFileMap.clear();
    }
}
