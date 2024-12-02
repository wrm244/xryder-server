package cn.xryder.base.repo.system;

import cn.xryder.base.domain.entity.system.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @Author: joetao
 * @Date: 2024/9/13 10:09
 */
public interface OperationLogRepo extends JpaRepository<OperationLog, Long> {
    Page<OperationLog> findByOperatorContaining(String q, Pageable pageable);
}
