package cn.xryder.base.controller.system;

import cn.xryder.base.config.OperationLog;
import cn.xryder.base.domain.PageResult;
import cn.xryder.base.domain.ResultJson;
import cn.xryder.base.domain.dto.system.PositionDTO;
import cn.xryder.base.domain.vo.PositionVO;
import cn.xryder.base.service.system.PositionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/9/19 14:17
 */
@RestController
@RequestMapping("/api/v1/positions")
public class PositionController {
    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system')")
    public ResultJson<PageResult<List<PositionVO>>> getPositionPageable(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long deptId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        if (page <= 1) {
            page = 1;
        }
        PageResult<List<PositionVO>> positions = positionService.queryPositions(q, deptId, page, pageSize);
        return ResultJson.ok(positions);
    }

    @OperationLog("添加职位")
    @PostMapping
    @PreAuthorize("hasAuthority('system')")
    public ResultJson<?> addPosition(@Valid @RequestBody PositionDTO position, Principal principal) {
        return ResultJson.ok(positionService.addPosition(position, principal.getName()));
    }

    @OperationLog("修改职位信息")
    @PutMapping
    @PreAuthorize("hasAuthority('system')")
    public ResultJson<?> updatePosition(@Valid @RequestBody PositionDTO position) {
        return ResultJson.ok(positionService.updatePosition(position));
    }

    @OperationLog("删除职位")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system')")
    public ResultJson<?> deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
        return ResultJson.ok();
    }
}
