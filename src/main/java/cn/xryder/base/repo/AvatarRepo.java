package cn.xryder.base.repo;

import cn.xryder.base.domain.entity.system.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @Author: joetao
 * @Date: 2024/8/16 14:20
 */
public interface AvatarRepo extends JpaRepository<Avatar, Long> {
}
