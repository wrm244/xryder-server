package cn.xryder.base.config.secrutiy;

import cn.xryder.base.exception.custom.BadRequestException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 全局校验pageSize参数，防止请求数量过大，数据泄露...
 */
@ControllerAdvice
public class GlobalPaginationValidator {
    private static final int MAX_PAGE_SIZE = 100;

    @ModelAttribute
    public void validatePagination(@RequestParam(required = false) Integer pageSize) {
        if (pageSize != null && (pageSize <= 0 || pageSize > MAX_PAGE_SIZE)) {
            throw new BadRequestException("无效参数 pageSize: 必须在 1 和 " + MAX_PAGE_SIZE + " 之间");
        }
    }
}
