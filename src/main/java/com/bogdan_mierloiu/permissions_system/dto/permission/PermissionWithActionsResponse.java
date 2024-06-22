package com.bogdan_mierloiu.permissions_system.dto.permission;

import com.bogdan_mierloiu.permissions_system.entity.Action;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Builder
@Schema(name = "PermissionWithActionsResponse", description = "Permission with actions response object")
public record PermissionWithActionsResponse(

        @Schema(description = "UUID of the permission")
        UUID uuid,

        @Schema(description = "Name of the permission")
        String name,

        @Schema(description = "Url of the permission")
        String url,

        @Schema(description = "Description of the permission")
        Set<Action> actions
) {
}
