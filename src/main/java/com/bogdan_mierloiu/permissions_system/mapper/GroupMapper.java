package com.bogdan_mierloiu.permissions_system.mapper;

import com.bogdan_mierloiu.permissions_system.dto.group.GroupResponse;
import com.bogdan_mierloiu.permissions_system.dto.group.GroupSimplifiedResponse;
import com.bogdan_mierloiu.permissions_system.entity.Group;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupMapper {

    private GroupMapper() {
    }


    public static GroupSimplifiedResponse entityToSimplifiedDto(Group group) {
        return GroupSimplifiedResponse.builder()
                .name(group.getName())
                .uuid(group.getUuid())
                .description(group.getDescription())
                .parentUuid(Optional.ofNullable(group.getParent())
                        .map(Group::getUuid)
                        .orElse(null))
                .enabled(group.getEnabled())
                .inheritPermissions(group.getInheritPermissions())
                .uuid(group.getUuid())
                .build();
    }

    public static GroupResponse entityToDto(Group group) {
        return GroupResponse.builder()
                .name(group.getName())
                .uuid(group.getUuid())
                .description(group.getDescription())
                .parentUuid(Optional.ofNullable(group.getParent())
                        .map(Group::getUuid)
                        .orElse(null))
                .enabled(group.getEnabled())
                .inheritPermissions(group.getInheritPermissions())
                .children(group.getChildren().stream()
                        .map(GroupMapper::entityToDto)
                        .collect(Collectors.toSet()))
                .users(group.getUsers().stream()
                        .map(UserMapper::entityToDto)
                        .collect(Collectors.toSet()))
                .build();
    }

    public static Set<GroupSimplifiedResponse> entityListToDto(Set<Group> groupDtoList) {
        return groupDtoList.stream()
                .map(GroupMapper::entityToSimplifiedDto)
                .collect(Collectors.toSet());
    }
}
