package cn.xryder.base.repo.system;

import cn.xryder.base.domain.entity.system.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/8/2 10:39
 */
public interface DepartmentRepo extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {
    List<Department> findDepartmentsByParentDepartmentIsNull();

    List<Department> findDepartmentsByParentDepartment(Department parentDepartment);

}
