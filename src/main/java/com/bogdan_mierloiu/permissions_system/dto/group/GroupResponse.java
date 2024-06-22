package com.bogdan_mierloiu.permissions_system.dto.group;

import com.bogdan_mierloiu.permissions_system.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Builder
public record GroupResponse(

        @Schema(description = "The group's name")
        String name,

        @Schema(description = "The group's uuid")
        UUID uuid,

        @Schema(description = "The group's description")
        String description,

        @Schema(description = "The group's parent uuid")
        UUID parentUuid,

        @Schema(description = "The group's enabled status")
        Boolean enabled,

        @Schema(description = "Inherit permissions from parent group. Default is false.")
        Boolean inheritPermissions,

        @Schema(description = "The group's children")
        Set<GroupResponse> children,

        @Schema(description = "The group's users")
        Set<UserResponse> users
) {
}
