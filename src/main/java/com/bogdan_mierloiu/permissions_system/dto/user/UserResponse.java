package com.bogdan_mierloiu.permissions_system.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Schema(description = "User response")
@Builder
public record UserResponse(

        @Schema(description = "User email")
        String email,

        @Schema(description = "User name")
        String name,

        @Schema(description = "User surname")
        String surname,

        @Schema(description = "Uuid of the user")
        UUID uuid

) {
}
