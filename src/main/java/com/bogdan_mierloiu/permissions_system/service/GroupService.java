package com.bogdan_mierloiu.permissions_system.service;

import com.bogdan_mierloiu.permissions_system.dto.group.GroupRequest;
import com.bogdan_mierloiu.permissions_system.dto.group.GroupSimplifiedResponse;
import com.bogdan_mierloiu.permissions_system.dto.group.UserGroupFailedMap;
import com.bogdan_mierloiu.permissions_system.dto.group.UserGroupRequestMap;
import com.bogdan_mierloiu.permissions_system.dto.permission.PermissionWithActionsRequest;
import com.bogdan_mierloiu.permissions_system.dto.permission.PermissionWithActionsResponse;
import com.bogdan_mierloiu.permissions_system.entity.*;
import com.bogdan_mierloiu.permissions_system.mapper.GroupMapper;
import com.bogdan_mierloiu.permissions_system.mapper.PermissionMapper;
import com.bogdan_mierloiu.permissions_system.repo.GroupRepo;
import com.bogdan_mierloiu.permissions_system.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepo groupRepo;
    private final UserRepo userRepo;
    private final GroupPermissionService groupPermissionService;

    @Transactional
    public GroupSimplifiedResponse save(GroupRequest groupRequest) {
        validateUniqueNameBeforeSave(groupRequest.parentUuid(), groupRequest.name());
        Group group = Group.builder()
                .name(groupRequest.name())
                .description(groupRequest.description())
                .enabled(Optional.ofNullable(groupRequest.enabled()).orElse(true))
                .inheritPermissions(Optional.ofNullable(groupRequest.inheritPermissions()).orElse(false))
                .groupPermissions(new HashSet<>())
                .children(new HashSet<>())
                .users(new HashSet<>())
                .build();

        Optional.ofNullable(groupRequest.parentUuid())
                .flatMap(groupRepo::findByUuid)
                .ifPresent(parentGroup -> {
                    group.setParent(parentGroup);
                    addUsersToGroupRecursivelyUp(group, parentGroup);
                });

        Group initialGroupSaved = groupRepo.save(group);
        if (nonNull(initialGroupSaved.getParent()) &&
                notEmpty(initialGroupSaved.getParent().getGroupPermissions()) &&
                Boolean.TRUE.equals(initialGroupSaved.getInheritPermissions())) {
            addPermissionsFromParent(initialGroupSaved, group.getParent().getGroupPermissions());
        }
        return GroupMapper.entityToSimplifiedDto(groupRepo.save(initialGroupSaved));
    }

    public Set<GroupSimplifiedResponse> getAllRootGroups() {
        return GroupMapper.entityListToDto(new HashSet<>(groupRepo.findRootGroups()));
    }


    public Optional<GroupSimplifiedResponse> getByUuid(UUID uuid) {
        return groupRepo.findByUuid(uuid)
                .map(GroupMapper::entityToSimplifiedDto);
    }

//    @Transactional
//    public GroupResponse update(UUID groupToUpdateUuid, GroupRequest groupRequest) {
//        Group groupToUpdate = groupRepo.findByUuid(groupToUpdateUuid).orElseThrow(() ->
//                new NotFoundException(ErrorMessages.objectWithUuidNotFound(GROUP, groupToUpdateUuid)));
//
//
//        checkRequestedNameAndUpdate(groupRequest.name(), groupToUpdate.getName(), groupToUpdate);
//        checkRequestedParentAndUpdate(groupRequest.parentUuid(), groupToUpdate);
//        checkRequestedInheritPermissionsAndUpdate(groupRequest.inheritPermissions(), groupToUpdate);
//
//        Group groupSaved = groupRepo.saveAndFlush(groupToUpdate);
//        managePermissionsAfterGroupUpdate(groupSaved);
//
//        Group updatedGroup = groupRepo.save(groupSaved);
//
//        GroupResponse updatedGroupDTO = GroupMapper.entityToDto(updatedGroup);
//        addChildRecursively(updatedGroupDTO, new HashSet<>());
//        return updatedGroupDTO;
//    }

    /**
     * Groups-users operations.
     */

    @Transactional
    public List<UserGroupFailedMap> addUsersToGroups(List<UserGroupRequestMap> userGroupMapList) {
        return userGroupMapList.stream()
                .map(this::processUserGroupPairAdd)
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional
    public List<UserGroupFailedMap> removeUsersFromGroups(List<UserGroupRequestMap> userGroupMapList) {
        return userGroupMapList.stream()
                .map(this::processUserGroupPairRemove)
                .filter(Objects::nonNull)
                .toList();
    }


    public UserGroupFailedMap processUserGroupPairAdd(UserGroupRequestMap userGroupPair) {
        List<Group> groupsToUpdate = new ArrayList<>();
        Optional<Group> group = groupRepo.findByUuid(userGroupPair.groupUuid());
        Optional<User> user = userRepo.findByUuid(userGroupPair.userUuid());

        if (group.isEmpty() || user.isEmpty()) {
            return UserGroupFailedMap.builder()
                    .message("Group or user does not exist.")
                    .userUuid(userGroupPair.userUuid())
                    .groupUuid(userGroupPair.groupUuid())
                    .build();
        }
        if (checkGroupContainsUser(group.get(), user.get())) {
            return UserGroupFailedMap.builder()
                    .message("User is already part of the group.")
                    .userUuid(userGroupPair.userUuid())
                    .groupUuid(userGroupPair.groupUuid())
                    .build();
        }
        addGroupsToUserRecursively(user.get(), group.get());
        groupsToUpdate.add(group.get());
        groupRepo.saveAll(groupsToUpdate);
        return null;
    }


    public UserGroupFailedMap processUserGroupPairRemove(UserGroupRequestMap userGroupPair) {
        List<Group> groupsToUpdate = new ArrayList<>();
        Optional<Group> group = groupRepo.findByUuid(userGroupPair.groupUuid());
        Optional<User> user = userRepo.findByUuid(userGroupPair.userUuid());

        if (group.isEmpty() || user.isEmpty()) {
            return UserGroupFailedMap.builder()
                    .message("Group or user does not exist.")
                    .userUuid(userGroupPair.userUuid())
                    .groupUuid(userGroupPair.groupUuid())
                    .build();
        }
        if (!checkGroupContainsUser(group.get(), user.get())) {
            return UserGroupFailedMap.builder()
                    .message("User is not part of the group.")
                    .userUuid(userGroupPair.userUuid())
                    .groupUuid(userGroupPair.groupUuid())
                    .build();
        }
        removeGroupsFromUserRecursively(user.get(), group.get());
        groupsToUpdate.add(group.get());
        groupRepo.saveAll(groupsToUpdate);
        return null;
    }

    private void addGroupsToUserRecursively(User appUser, Group group) {
        appUser.getGroups().add(group);
        Set<Group> groupChildren = group.getChildren();
        for (Group child : groupChildren) {
            addGroupsToUserRecursively(appUser, child);
        }
    }

    private void removeGroupsFromUserRecursively(User appUser, Group group) {
        appUser.getGroups().remove(group);
        Set<Group> groupChildren = group.getChildren();
        for (Group child : groupChildren) {
            addGroupsToUserRecursively(appUser, child);
        }
    }


    private void addUsersToGroupRecursivelyUp(Group groupToUpdate, Group parentGroup) {
        Set<User> users = parentGroup.getUsers();
        groupToUpdate.getUsers().addAll(users);

        Optional.ofNullable(parentGroup.getParent())
                .ifPresent(parentNextLevel -> addUsersToGroupRecursivelyUp(groupToUpdate, parentNextLevel));
    }

    /**
     * Groups-permissions operations.
     */

    @Transactional
    public List<PermissionWithActionsResponse> updateGroupPermissions(UUID uuid, List<PermissionWithActionsRequest> permissionWithActionsRequests) {
        Group groupToUpdate = groupRepo.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Group with uuid " + uuid + " does not exist."));

        return permissionWithActionsRequests.stream()
                .map(permissionRequestObject -> processAddPermissionsToGroup(groupToUpdate, permissionRequestObject, false))
                .toList();
    }

    private PermissionWithActionsResponse processAddPermissionsToGroup(Group groupToUpdate, PermissionWithActionsRequest permissionWithActionsRequest, boolean needClear) {
        Permission permissionToUse = validateAndGetPermission(permissionWithActionsRequest.permissionUuid());
        Set<Action> actions = permissionWithActionsRequest.actions().stream()
                .map(groupPermissionService::getActionByUuid)
                .collect(Collectors.toSet());
        return updatePermissionsToGroupsRecursively(groupToUpdate, permissionToUse, actions, needClear);
    }

    private PermissionWithActionsResponse updatePermissionsToGroupsRecursively(Group group, Permission permission, Set<Action> actions, boolean needClear) {
        if (needClear) {
            group.getGroupPermissions().clear();
        }
        Optional<GroupPermission> optionalGroupPermission = groupPermissionService.getByGroupAndPermission(group, permission);
        GroupPermission groupPermission;
        if (optionalGroupPermission.isPresent()) {
            groupPermission = optionalGroupPermission.get();
            groupPermission.getActions().clear();
            groupPermission.getActions().addAll(actions);
            groupPermissionService.save(groupPermission);
        } else {
            groupPermission = GroupPermission.builder()
                    .group(group)
                    .permission(permission)
                    .actions(actions)
                    .build();
            groupPermissionService.save(groupPermission);
            group.getGroupPermissions().add(groupPermission);
        }
        Group savedGroup = groupRepo.saveAndFlush(group);
        Set<Group> groupChildren = savedGroup.getChildren();
        for (Group child : groupChildren) {
            if (Boolean.FALSE.equals(child.getInheritPermissions())) {
                return PermissionMapper.mapToPermissionWithActionsResponse(
                        GroupPermission.builder()
                                .group(group)
                                .permission(permission)
                                .actions(actions)
                                .build());
            }
            updatePermissionsToGroupsRecursively(child, permission, actions, needClear);
        }
        return PermissionMapper.mapToPermissionWithActionsResponse(groupPermission);
    }

    private void addPermissionsFromParent(Group groupToUpdate, Set<GroupPermission> groupPermissions) {
        if (notEmpty(groupToUpdate.getGroupPermissions())) {
            groupToUpdate.getGroupPermissions().clear();
        }
        groupPermissions.stream().map(
                        groupPermission -> GroupPermission.builder()
                                .group(groupToUpdate)
                                .permission(groupPermissionService.getPermissionsByUuid(groupPermission.getPermission().getUuid()))
                                .actions(groupPermission.getActions().stream()
                                        .map(action -> groupPermissionService.getActionByUuid(action.getUuid()))
                                        .collect(Collectors.toSet()))
                                .build())
                .forEach(groupPermission -> {
                    groupPermissionService.save(groupPermission);
                    groupToUpdate.getGroupPermissions().add(groupPermission);
                });
    }

    // Validation methods
    private Permission validateAndGetPermission(UUID permissionUuid) {
        return groupPermissionService.getPermissionsOptionalsByUuid(permissionUuid)
                .orElseThrow(() -> new IllegalArgumentException("Permission with uuid " + permissionUuid + " does not exist."));
    }

    private boolean checkGroupContainsUser(Group group, User user) {
        return user.getGroups().contains(group);
    }

    private void validateUniqueNameBeforeSave(UUID parentUuid, String requestedName) {
        Optional.ofNullable(parentUuid)
                .ifPresentOrElse(uuid -> validateUniqueNameForSubgroup(uuid, requestedName),
                        () -> validateUniqueName(requestedName));
    }

    private void validateUniqueNameForSubgroup(UUID parentUuid, String name) {
        Optional<Group> parentGroup = groupRepo.findByUuid(parentUuid);
        parentGroup.ifPresentOrElse(group -> checkUniqueNameInList(name, group.getChildren()),
                () -> {
                    throw new IllegalArgumentException("Parent group with uuid " + parentUuid + " does not exist.");
                });
    }

    private void validateUniqueName(String name) {
        if (groupRepo.findRootGroupsByName(name).stream()
                .anyMatch(group -> group.getName().equals(name))) {
            throw new IllegalArgumentException("Group with name " + name + " already exists.");
        }
    }

    private void checkUniqueNameInList(String name, Set<Group> groupList) {
        if (groupList.stream()
                .anyMatch(group -> group.getName().equals(name))) {
            throw new IllegalArgumentException("Group with name " + name + " already exists.");
        }
    }

    private static boolean notEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
}
