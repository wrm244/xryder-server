package cn.xryder.base.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/10/9 8:37
 */
@Data
public class ChatMessageDTO {
    private String message;
    private String conversationId;
    private List<String> fileNames;
}
