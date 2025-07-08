package cn.xryder.base.config.secrutiy;
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

import cn.xryder.base.exception.custom.BadRequestException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 全局校验pageSize参数，防止请求数量过大，数据泄露...
 *
 * @Author: joetao
 * @Date: 2024/12/11 10:09
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
