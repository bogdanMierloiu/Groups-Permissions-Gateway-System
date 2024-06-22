package com.bogdan_mierloiu.permissions_system.dto.group;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserGroupFailedMap(

        String message,

        @NotNull
        UUID userUuid,

        @NotNull
        UUID groupUuid
) {
}
