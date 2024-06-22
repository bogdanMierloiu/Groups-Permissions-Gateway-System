package com.bogdan_mierloiu.permissions_system.controller;

import com.bogdan_mierloiu.permissions_system.dto.ResponseDto;
import com.bogdan_mierloiu.permissions_system.dto.group.UserGroupFailedMap;
import com.bogdan_mierloiu.permissions_system.dto.group.UserGroupRequestMap;
import com.bogdan_mierloiu.permissions_system.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("group-users")
@RequiredArgsConstructor
public class GroupUsersController {

    private final GroupService groupService;

    @Operation(summary = "Add users to groups", description = "Associates users with respective groups and their subgroups. "
            + "This endpoint attempts to associate users with groups based on provided mappings. "
            + "Additionally, users will be associated with all subgroups of the given parent group. "
            + "If a group or user does not exist or the user is already associated with the group," +
            " the operation will fail for that user-group pair and the response will contain the failed mappings.",
            operationId = "addUsersToGroup")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All users added to groups",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserGroupFailedMap.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized for this action"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))})})
    @PostMapping
    public ResponseEntity<List<UserGroupFailedMap>> addUsersToGroup(
            @RequestBody
            @Valid
            List<UserGroupRequestMap> userGroupMapList) {
        List<UserGroupFailedMap> userGroupFailedMaps = groupService.addUsersToGroups(userGroupMapList);
        return userGroupFailedMaps.isEmpty() ?
                ResponseEntity.ok().build() :
                ResponseEntity.accepted().body(userGroupFailedMaps);
    }

    @Operation(summary = "Remove users from groups", description = "Removes users from respective groups. "
            + "This endpoint attempts to remove users from groups based on provided mappings. ",
            operationId = "removeUsersFromGroup")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All users removed from groups",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserGroupFailedMap.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized for this action"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDto.class))})})
    @DeleteMapping
    public ResponseEntity<List<UserGroupFailedMap>> removeUsersFromGroups(
            @RequestBody
            @Valid
            List<UserGroupRequestMap> userGroupMapList) {
        List<UserGroupFailedMap> userGroupFailedMaps = groupService.removeUsersFromGroups(userGroupMapList);
        return userGroupFailedMaps.isEmpty() ?
                ResponseEntity.ok().build() :
                ResponseEntity.accepted().body(userGroupFailedMaps);
    }

}
