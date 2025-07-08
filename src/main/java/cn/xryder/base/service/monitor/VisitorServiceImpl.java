package cn.xryder.base.service.monitor;
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

import cn.xryder.base.domain.entity.monitor.VisitorRecordDO;
import cn.xryder.base.repo.monitor.VisitorRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * @Author: joetao
 * @Date: 2024/12/3 16:16
 */
@Service
public class VisitorServiceImpl implements VisitorService {
    private final VisitorRepo visitorRepo;

    public VisitorServiceImpl(VisitorRepo visitorRepo) {
        this.visitorRepo = visitorRepo;
    }

    @Override
    public void recordVisit(String userUUID) {
        LocalDate today = LocalDate.now();
        if (!visitorRepo.existsByUserUUIDAndVisitDate(userUUID, today)) {
            visitorRepo.save(new VisitorRecordDO(userUUID, today));
        }
    }

    @Override
    public Long countUniqueVisitors(LocalDate localDate) {
        return visitorRepo.countUniqueVisitors(localDate);
    }
}
