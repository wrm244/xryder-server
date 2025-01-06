package cn.xryder.base.controller.system;

import cn.xryder.base.config.OperationLog;
import cn.xryder.base.domain.ResultJson;
import cn.xryder.base.domain.dto.system.DeptDTO;
import cn.xryder.base.domain.entity.system.Department;
import cn.xryder.base.domain.vo.DepartmentVO;
import cn.xryder.base.service.system.DepartmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * @Author: joetao
 * @Date: 2024/8/2 10:46
 */
@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ResultJson<DepartmentVO> getAllDepartments(@RequestParam(required = false) String q) {
        return ResultJson.ok(departmentService.getAllDepartments(q));
    }

    @GetMapping("/{id}")
    public ResultJson<DepartmentVO> getDepartmentById(@PathVariable Long id) {
        return ResultJson.ok(departmentService.getDepartmentById(id));
    }

    @OperationLog("创建部门")
    @PostMapping
    @PreAuthorize("hasAuthority('system')")
    public ResultJson<DepartmentVO> createDepartment(@RequestBody Department department, Principal principal) {
        return ResultJson.ok(departmentService.createDepartment(department, principal.getName()));
    }

    @OperationLog("部门排序")
    @PutMapping("/position")
    @PreAuthorize("hasAuthority('system')")
    public ResultJson<DepartmentVO> moveDepartment(
            @RequestParam Long id, @RequestParam Long parentId) {
        DepartmentVO updatedDepartment = departmentService.moveDepartment(id, parentId);
        return ResultJson.ok(updatedDepartment);
    }

    @OperationLog("修改部门信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system')")
    public ResultJson<DepartmentVO> updateDepartment(
            @PathVariable Long id, @RequestBody DeptDTO deptDTO) {
        DepartmentVO updatedDepartment = departmentService.updateDepartment(id, deptDTO);
        return ResultJson.ok(updatedDepartment);
    }

    @OperationLog("删除部门")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system')")
    public ResultJson<?> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResultJson.ok();
    }
}
