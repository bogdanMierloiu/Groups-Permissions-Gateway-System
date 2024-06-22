package com.bogdan_mierloiu.permissions_system.service;

import com.bogdan_mierloiu.permissions_system.entity.Group;
import com.bogdan_mierloiu.permissions_system.entity.GroupPermission;
import com.bogdan_mierloiu.permissions_system.entity.User;
import com.bogdan_mierloiu.permissions_system.repo.RoleRepo;
import com.bogdan_mierloiu.permissions_system.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final RoleRepo roleRepo;
    private final UserRepo appUserRepo;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public User save(User userToSave, String roleName) {
        userToSave.setRole(roleRepo.findByName(roleName));
        return appUserRepo.save(userToSave);
    }

    public User getUserByEmail(String email) {
        return appUserRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getAppUserFromSecurityContext() {
        OAuth2AuthenticatedPrincipal authenticatedPrincipal = getAuthenticatedPrincipal();
        String email = authenticatedPrincipal.getAttribute("email");
        return getUserByEmail(email);
    }

    public static OAuth2AuthenticatedPrincipal getAuthenticatedPrincipal() {
        return (OAuth2AuthenticatedPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public static Set<GroupPermission> getPermissionsForUser(User user) {
        return user.getGroups().stream()
                .map(Group::getGroupPermissions)
                .map(groupPermissions -> groupPermissions.stream().filter(groupPermission -> !groupPermission.getActions().isEmpty()).toList())
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

}
