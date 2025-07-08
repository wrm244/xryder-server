package cn.xryder.base.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/8/20 15:20
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class RoleVO {
    private Long id;
    private String name;
    private Integer level;
    private Integer type;
    private String remark;
    private List<Long> permissions;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
