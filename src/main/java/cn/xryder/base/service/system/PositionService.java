package cn.xryder.base.service.system;

import cn.xryder.base.domain.PageResult;
import cn.xryder.base.domain.dto.system.PositionDTO;
import cn.xryder.base.domain.vo.PositionVO;

import java.util.List;

/**
 * 职位管理
 *
 * @Author: joetao
 * @Date: 2024/9/19 14:24
 */
public interface PositionService {
    /**
     * 添加职位
     *
     * @param position 职位信息
     * @return 职位信息
     */
    PositionVO addPosition(PositionDTO position, String username);

    /**
     * 分页查询
     *
     * @param q        搜索条件
     * @param deptId   部门id
     * @param page     第几页
     * @param pageSize 每页条数
     * @return 分页数据
     */
    PageResult<List<PositionVO>> queryPositions(String q, Long deptId, int page, int pageSize);

    /**
     * 更新职位信息
     *
     * @param position 职位信息
     * @return 职位信息
     */
    PositionVO updatePosition(PositionDTO position);

    /**
     * 删除职位
     *
     * @param id 职位id
     */
    void deletePosition(Long id);
}
