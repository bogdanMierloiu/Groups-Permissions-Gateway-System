package com.bogdan_mierloiu.permissions_system.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(name = "PermissionResponse", description = "Permission response object")
public record PermissionResponse(

        @Schema(description = "UUID of the permission")
        UUID uuid,

        @Schema(description = "Name of the permission")
        String name,

        @Schema(description = "Url of the permission")
        String url,

        @Schema(description = "Description of the permission")
        String description

) {
}
