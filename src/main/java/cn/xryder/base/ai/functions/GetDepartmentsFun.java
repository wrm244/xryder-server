package cn.xryder.base.ai.functions;

import cn.xryder.base.domain.entity.system.Department;
import cn.xryder.base.repo.system.DepartmentRepo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 部门查询函数
 *
 * @Author: joetao
 * @Date: 2024/10/15 8:53
 */
@Slf4j
public class GetDepartmentsFun implements Function<GetDepartmentsFun.Request, GetDepartmentsFun.Response> {
    private final DepartmentRepo departmentRepo;

    public GetDepartmentsFun(DepartmentRepo departmentRepo) {
        this.departmentRepo = departmentRepo;
    }

    @Override
    public Response apply(Request request) {
        Long deptId = request.deptId;
        if (deptId == null) {
            deptId = 1L;
        }
        log.info("查询部门id: {}", deptId);
        Department parentDepartment = new Department();
        parentDepartment.setId(deptId);
        List<Department> departments = departmentRepo.findDepartmentsByParentDepartment(parentDepartment);
        List<DepartmentRecord> departmentRecords = new ArrayList<>();
        for (Department child : departments) {
            departmentRecords.add(new DepartmentRecord(child.getId(), child.getName()));
        }
        return new Response(departmentRecords);
    }

    public record Request(Long deptId) {

    }

    public record DepartmentRecord(Long id, String name) {
    }

    public record Response(List<DepartmentRecord> departments) {
    }

}
