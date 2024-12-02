package cn.xryder.base.repo.system;

import cn.xryder.base.domain.entity.system.LoginLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @Author: joetao
 * @Date: 2024/9/12 10:59
 */
public interface LoginLogRepo extends JpaRepository<LoginLog, Long> {
    Page<LoginLog> findByUsernameContaining(String q, Pageable pageable);
}
