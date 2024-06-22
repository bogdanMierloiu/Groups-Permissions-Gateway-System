package com.bogdan_mierloiu.permissions_system.controller;

import com.bogdan_mierloiu.permissions_system.dto.group.GroupRequest;
import com.bogdan_mierloiu.permissions_system.dto.group.GroupSimplifiedResponse;
import com.bogdan_mierloiu.permissions_system.dto.ResponseDto;
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
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "Create a new group",
            description = "Allows the creation of a new group, optionally associating it as a child of an existing group. " +
                    "This operation also enables the automatic inheritance of users from the parent group hierarchy.",
            operationId = "saveGroup")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group created",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GroupSimplifiedResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized for this action",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))})})
    @PostMapping
    public ResponseEntity<GroupSimplifiedResponse> saveGroup(
            @RequestBody
            @Valid
            GroupRequest groupRequest) {
        return ResponseEntity.ok().body(groupService.save(groupRequest));
    }

    @Operation(summary = "The list of root groups",
            description = "Returns an alphabetically sorted list of root groups.",
            operationId = "getAllRootGroups")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of groups",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = GroupSimplifiedResponse.class)))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized for this action"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))})})
    @GetMapping
    public ResponseEntity<Set<GroupSimplifiedResponse>> getAllRootGroups() {
        return ResponseEntity.ok().body(groupService.getAllRootGroups());
    }

    @Operation(summary = "Get group by UUID", description = "Get group details by UUID",
            operationId = "getGroupByUuid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = GroupSimplifiedResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Group does not exist"),
            @ApiResponse(responseCode = "401", description = "Unauthorized for this action"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))})})
    @GetMapping("{uuid}")
    public ResponseEntity<GroupSimplifiedResponse> getGroupByUuid(@PathVariable("uuid") UUID uuid) {
        return groupService.getByUuid(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
