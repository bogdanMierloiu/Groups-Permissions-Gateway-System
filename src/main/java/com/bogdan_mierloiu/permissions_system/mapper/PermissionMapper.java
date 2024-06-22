package com.bogdan_mierloiu.permissions_system.mapper;

import com.bogdan_mierloiu.permissions_system.dto.permission.PermissionResponse;
import com.bogdan_mierloiu.permissions_system.dto.permission.PermissionWithActionsResponse;
import com.bogdan_mierloiu.permissions_system.entity.Action;
import com.bogdan_mierloiu.permissions_system.entity.GroupPermission;
import com.bogdan_mierloiu.permissions_system.entity.Permission;

import java.util.List;
import java.util.Set;

public class PermissionMapper {

    private PermissionMapper() {
    }

    public static PermissionResponse entityToDto(Permission permission) {
        return PermissionResponse.builder()
                .url(permission.getUrl())
                .name(permission.getName())
                .description(permission.getDescription())
                .uuid(permission.getUuid())
                .build();
    }

    public static List<PermissionResponse> entityListToDto(List<Permission> permissions) {
        return permissions.stream()
                .map(PermissionMapper::entityToDto)
                .toList();
    }

    public static PermissionWithActionsResponse mapToPermissionWithActionsResponse(GroupPermission groupPermission) {
        return PermissionWithActionsResponse.builder()
                .uuid(groupPermission.getPermission().getUuid())
                .name(groupPermission.getPermission().getName())
                .url(groupPermission.getPermission().getUrl())
                .actions(groupPermission.getActions())
                .build();
    }

    public static PermissionWithActionsResponse mapToPermissionWithActionsResponse(Permission permission, Set<Action> actions) {
        return PermissionWithActionsResponse.builder()
                .uuid(permission.getUuid())
                .name(permission.getName())
                .url(permission.getUrl())
                .actions(actions)
                .build();
    }

}