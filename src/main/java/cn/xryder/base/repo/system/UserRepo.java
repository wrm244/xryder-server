package cn.xryder.base.repo.system;

import cn.xryder.base.domain.entity.system.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/7/30 16:22
 */
@Repository
public interface UserRepo extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    List<User> findAllByDepartmentIdIn(List<Long> deptIds);
}
