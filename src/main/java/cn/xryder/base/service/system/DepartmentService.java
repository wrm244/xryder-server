package cn.xryder.base.service.system;

import cn.xryder.base.domain.dto.system.DeptDTO;
import cn.xryder.base.domain.entity.system.Department;
import cn.xryder.base.domain.vo.DepartmentVO;

/**
 * 部门机构管理
 * @Author: joetao
 * @Date: 2024/8/2 10:40
 */
public interface DepartmentService {
    /**
     * 查询所有部门
     * @param q 查询条件，模糊搜索部门名称
     * @return 部门信息
     */
    DepartmentVO getAllDepartments(String q);

    /**
     * 根据部门id查询部门信息
     * @param id 部门id
     * @return 部门信息
     */
    DepartmentVO getDepartmentById(Long id);

    /**
     * 创建部门
     * @param department 部门信息
     * @return 部门信息
     */
    DepartmentVO createDepartment(Department department, String username);

    /**
     * 移动部门
     * @param id 部门id
     * @param parentId 上级部门id
     * @return 部门信息
     */
    DepartmentVO moveDepartment(Long id, Long parentId);

    /**
     * 更新部门信息
     * @param id 部门id
     * @param deptDTO 更新信息
     * @return 部门信息
     */
    DepartmentVO updateDepartment(Long id, DeptDTO deptDTO);

    /**
     * 删除部门
     * @param id 部门id
     */
    void deleteDepartment(Long id);


}
