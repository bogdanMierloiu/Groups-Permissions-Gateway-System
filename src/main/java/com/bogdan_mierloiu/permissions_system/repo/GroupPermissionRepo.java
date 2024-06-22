package com.bogdan_mierloiu.permissions_system.repo;

import com.bogdan_mierloiu.permissions_system.entity.Group;
import com.bogdan_mierloiu.permissions_system.entity.GroupPermission;
import com.bogdan_mierloiu.permissions_system.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupPermissionRepo extends JpaRepository<GroupPermission, Long> {

    List<GroupPermission> findByPermissionId(Long id);

    Optional<GroupPermission> findByGroupAndPermission(Group group, Permission permission);

}
