package com.bogdan_mierloiu.permissions_system.controller;


import com.bogdan_mierloiu.permissions_system.dto.ResponseDto;
import com.bogdan_mierloiu.permissions_system.dto.permission.PermissionWithActionsRequest;
import com.bogdan_mierloiu.permissions_system.dto.permission.PermissionWithActionsResponse;
import com.bogdan_mierloiu.permissions_system.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("group-permissions")
@RequiredArgsConstructor
@Validated
public class GroupPermissionsController {

    private final GroupService groupService;

    @Operation(summary = "Update permissions for a group", description = "Overrides permission from requested map with new actions.",
            operationId = "updateGroupPermissions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The group permissions updated",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PermissionWithActionsResponse.class)))}),
            @ApiResponse(responseCode = "404", description = "Group does not exist/Permission does not exist",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized for this action",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))})})
    @PatchMapping("{groupUuid}")
    public ResponseEntity<List<PermissionWithActionsResponse>> updateGroupPermissions(
            @PathVariable("groupUuid") UUID groupUuid,
            @RequestBody @Valid List<PermissionWithActionsRequest> permissionWithActionsRequests) {
        return ResponseEntity.ok().body(groupService.updateGroupPermissions(groupUuid, permissionWithActionsRequests));
    }

}
