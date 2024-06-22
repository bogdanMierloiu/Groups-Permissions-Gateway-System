package com.bogdan_mierloiu.permissions_system.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Represents a request object for the group.")
public record GroupRequest (

        @Schema(description = "The group's name. It must be unique and can be updated")
        @Size(min = 3, max = 35, message = "Name must be between 3 and 128 characters")
        @NotBlank(message = "Name is mandatory")
        @Pattern(regexp = "^(?=.*[a-zA-Z0-9])[a-zA-Z0-9`~!@#$%^&*()_\\-+={}\\[\\]:\";'<>?,./\\\\| ]+$",
                message = "Name must contain only characters from the keyboard and must include at least one alphanumeric character")
        String name,

        @Schema(description = "The group's description")
        @Pattern(regexp = "^(?=.*[a-zA-Z0-9])[a-zA-Z0-9`~!@#$%^&*()_\\-+={}\\[\\]:\";'<>?,./\\\\| ]+$",
                message = "Description must contain only characters from the keyboard and must include at least one alphanumeric character")
        @Size(min = 3, max = 512, message = "Description must be between 3 and 128 characters")
        String description,

        @Schema(description = "Parent uuid")
        UUID parentUuid,

        @Schema(description = "The group's enabled status")
        Boolean enabled,

        @Schema(description = "Inherit permissions from parent group. Default is false.")
        Boolean inheritPermissions

){

}
