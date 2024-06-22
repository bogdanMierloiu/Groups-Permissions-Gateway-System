package com.bogdan_mierloiu.permissions_system.config;

import com.bogdan_mierloiu.permissions_system.entity.Action;
import com.bogdan_mierloiu.permissions_system.entity.GroupPermission;
import com.bogdan_mierloiu.permissions_system.entity.User;
import com.bogdan_mierloiu.permissions_system.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.bogdan_mierloiu.permissions_system.service.UserService.getPermissionsForUser;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserPermissionsFilter extends OncePerRequestFilter {

    private final UserService appUserService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();

        if (!isUserAllowedToPerformRequest(requestURI, requestMethod)) {
            log.error("User does not have permission to access {} {}", requestMethod, requestURI);
            response.sendError(403, "You do not have permission to access this resource.");
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return PublicEndpoint.isUriPublic(requestURI)
                || requestURI.startsWith("/profile")
                || requestURI.startsWith("/ws-notifications")
                || requestURI.startsWith("/documentation");
    }

    private boolean isUserAllowedToPerformRequest(String requestUri, String requestHttpMethod) {
        User user = appUserService.getAppUserFromSecurityContext();
        Set<Action> userActionsForUrl = getPermissionsForUser(user).stream()
                .filter(groupPermission -> requestUri.startsWith(groupPermission.getPermission().getUrl()))
                .map(GroupPermission::getActions)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        Predicate<User> userIsSuperAdmin = appuser -> "SUPER_ADMIN".equals(appuser.getRole().getName()) || "ADMIN".equals(appuser.getRole().getName());
        Predicate<Set<Action>> userIsAllowedToPerformAction = actions -> actions.stream()
                .map(Action::getName)
                .map(Enum::name)
                .anyMatch(requestHttpMethod::equals);

        return (userIsSuperAdmin.test(user) || userIsAllowedToPerformAction.test(userActionsForUrl));
    }

}