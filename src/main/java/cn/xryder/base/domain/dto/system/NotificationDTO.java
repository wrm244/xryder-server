package cn.xryder.base.domain.dto.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/9/25 9:39
 */
@Data
public class NotificationDTO {
    @NotBlank(message = "必须要有通知标题")
    @Size(min = 2, max = 48, message = "通知名称长度在2-48之间")
    private String title;
    @NotBlank(message = "必须要有通知内容")
    @Size(min = 2, max = 2000, message = "通知内容长度在2-2000之间")
    private String content;
    /**
     * 1： 按部门发送，2：所有人，其他情况默认按所有人发布
     */
    private Integer type;
    private List<Long> deptIds;
}
