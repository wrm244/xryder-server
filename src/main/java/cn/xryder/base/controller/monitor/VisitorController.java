package cn.xryder.base.controller.monitor;
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

import cn.xryder.base.domain.ResultJson;
import cn.xryder.base.service.monitor.VisitorService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * @Author: joetao
 * @Date: 2024/12/3 16:14
 */
@RestController
@RequestMapping("/api/v1/visitor")
public class VisitorController {

    private final VisitorService visitorService;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @PostMapping("/visit")
    public void recordVisit(@RequestBody VisitorRequest request) {
        visitorService.recordVisit(request.getUserUUID());
    }

    @GetMapping("/uv")
    public ResultJson<Long> getDailyUV() {
        return ResultJson.ok(visitorService.countUniqueVisitors(LocalDate.now()));
    }
}

class VisitorRequest {
    private String userUUID;

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }
}
