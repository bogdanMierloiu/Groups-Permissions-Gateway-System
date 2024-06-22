package com.bogdan_mierloiu.permissions_system.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
@Schema(name = "PermissionWithActionRequest", description = "Add permission with actions to group request object")
public record PermissionWithActionsRequest(

        @NotNull(message = "Permission UUID is mandatory")
        @Schema(description = "UUID of the permission to be added/overridden with actions")
        UUID permissionUuid,

        @Schema(description = "List of action UUIDs to be added to the permission")
        List<UUID> actions
) {
}
