package com.bogdan_mierloiu.permissions_system.service;

import com.bogdan_mierloiu.permissions_system.entity.Action;
import com.bogdan_mierloiu.permissions_system.entity.Group;
import com.bogdan_mierloiu.permissions_system.entity.GroupPermission;
import com.bogdan_mierloiu.permissions_system.entity.Permission;
import com.bogdan_mierloiu.permissions_system.repo.ActionRepo;
import com.bogdan_mierloiu.permissions_system.repo.GroupPermissionRepo;
import com.bogdan_mierloiu.permissions_system.repo.PermissionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupPermissionService {

    private final GroupPermissionRepo groupPermissionRepo;
    private final PermissionRepo permissionRepo;
    private final ActionRepo actionRepo;

    @Transactional
    public GroupPermission save(GroupPermission groupPermission) {
        return groupPermissionRepo.save(groupPermission);
    }

    @Transactional(readOnly = true)
    public Optional<GroupPermission> getByGroupAndPermission(Group group, Permission permission) {
        return groupPermissionRepo.findByGroupAndPermission(group, permission);
    }

    @Transactional
    public void delete(GroupPermission groupPermission) {
        groupPermissionRepo.delete(groupPermission);
    }

    // PERMISSION & ACTION -> utils

    public Optional<Permission> getPermissionsOptionalsByUuid(UUID uuid) {
        return permissionRepo.findByUuid(uuid);
    }

    public Permission getPermissionsByUuid(UUID uuid) {
        return permissionRepo.findByUuid(uuid).orElseThrow(
                () -> new RuntimeException("Permission not found"));
    }

    public Action getActionByUuid(UUID uuid) {
        return actionRepo.findByUuid(uuid).orElseThrow(
                () -> new RuntimeException("Action not found"));
    }
}
