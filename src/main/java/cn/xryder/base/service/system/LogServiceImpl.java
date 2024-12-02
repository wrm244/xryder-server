package cn.xryder.base.service.system;

import cn.xryder.base.domain.PageResult;
import cn.xryder.base.domain.entity.system.LoginLog;
import cn.xryder.base.domain.entity.system.OperationLog;
import cn.xryder.base.domain.vo.LoginLogVO;
import cn.xryder.base.domain.vo.OperationLogVO;
import cn.xryder.base.repo.system.LoginLogRepo;
import cn.xryder.base.repo.system.OperationLogRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/9/12 15:02
 */
@Service
public class LogServiceImpl implements LogService {
    private final LoginLogRepo loginLogRepo;
    private final OperationLogRepo operationLogRepo;

    public LogServiceImpl(LoginLogRepo loginLogRepo, OperationLogRepo operationLogRepo) {
        this.loginLogRepo = loginLogRepo;
        this.operationLogRepo = operationLogRepo;
    }

    @Override
    public PageResult<List<LoginLogVO>> queryLoginLog(String q, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<LoginLog> all;
        if (StringUtils.isEmpty(q)) {
            all = loginLogRepo.findAll(pageable);
        } else {
            all = loginLogRepo.findByUsernameContaining(q, pageable);
        }
        List<LoginLogVO> loginLogs = new ArrayList<>();
        all.get().forEach(logDO -> {
            LoginLogVO loginLogVO = new LoginLogVO();
            BeanUtils.copyProperties(logDO, loginLogVO);
            loginLogs.add(loginLogVO);
        });
        return PageResult.<List<LoginLogVO>>builder().page(page).data(loginLogs).rows(loginLogs.size()).total(all.getTotalElements()).build();
    }

    public void saveOperationLog(String content, String methodName, String requestParams, String operator, long timeTaken) {
        OperationLog log = new OperationLog();
        log.setContent(content);
        log.setMethodName(methodName);
        log.setRequestParams(requestParams);
        log.setOperator(operator);
        log.setOperationTime(LocalDateTime.now());
        log.setTimeTaken(timeTaken);
        operationLogRepo.save(log);
    }

    @Override
    public PageResult<List<OperationLogVO>> queryOperationLog(String q, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<OperationLog> all;
        if (StringUtils.isEmpty(q)) {
            all = operationLogRepo.findAll(pageable);
        } else {
            all = operationLogRepo.findByOperatorContaining(q, pageable);
        }
        List<OperationLogVO> operationLogs = new ArrayList<>();
        all.get().forEach(logDO -> {
            OperationLogVO operationLogVO = new OperationLogVO();
            BeanUtils.copyProperties(logDO, operationLogVO);
            operationLogs.add(operationLogVO);
        });
        return PageResult.<List<OperationLogVO>>builder().page(page).data(operationLogs).rows(operationLogs.size()).total(all.getTotalElements()).build();

    }
}
