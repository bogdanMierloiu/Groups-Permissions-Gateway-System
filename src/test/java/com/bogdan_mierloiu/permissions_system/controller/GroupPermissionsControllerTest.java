package com.bogdan_mierloiu.permissions_system.controller;

import com.bogdan_mierloiu.permissions_system.config.ApplicationContextProvider;
import com.bogdan_mierloiu.permissions_system.dto.group.GroupRequest;
import com.bogdan_mierloiu.permissions_system.dto.group.UserGroupRequestMap;
import com.bogdan_mierloiu.permissions_system.dto.permission.PermissionResponse;
import com.bogdan_mierloiu.permissions_system.dto.permission.PermissionWithActionsRequest;
import com.bogdan_mierloiu.permissions_system.entity.Action;
import com.bogdan_mierloiu.permissions_system.entity.Group;
import com.bogdan_mierloiu.permissions_system.entity.HttpAction;
import com.bogdan_mierloiu.permissions_system.entity.User;
import com.bogdan_mierloiu.permissions_system.repo.ActionRepo;
import com.bogdan_mierloiu.permissions_system.repo.GroupRepo;
import com.bogdan_mierloiu.permissions_system.repo.UserRepo;
import com.bogdan_mierloiu.permissions_system.service.PermissionService;
import com.bogdan_mierloiu.permissions_system.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
@WebAppConfiguration
@ActiveProfiles("test")
class GroupPermissionsControllerTest {

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:16.3")
            .withDatabaseName("permissions_system")
            .withUsername("postgres")
            .withPassword("postgres");


    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GroupRepo groupRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserService appUserService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ActionRepo actionRepo;


    private User admin;
    private Group groupBeforeTest;

    @BeforeEach
    @Transactional
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        Optional<User> optionalAdmin = userRepo.findByEmail("java-admin-test@gmail.com");


        admin = optionalAdmin.orElseGet(() -> appUserService.save(User.builder()
                        .name("Test")
                        .surname("Admin")
                        .email("java-admin-test@gmail.com")
                        .groups(new HashSet<>())
                        .build(),
                "ADMIN"));

        groupBeforeTest = groupRepo.findAll().stream().findFirst().orElse(groupRepo.save(Group.builder()
                .name("Spring Boot Test Root Group")
                .description("Spring Boot Test Root Group Description")
                .parent(null)
                .children(new HashSet<>())
                .users(new HashSet<>())
                .groupPermissions(new HashSet<>())
                .enabled(true)
                .inheritPermissions(true)
                .uuid(UUID.randomUUID())
                .build()));
    }

    @AfterAll
    public static void tearDown() {
        ApplicationContext context = ApplicationContextProvider.getApplicationContext();
        GroupRepo groupRepo = context.getBean(GroupRepo.class);

        List<Group> springBootTests = groupRepo.findByNameContains("Spring Boot Test");
        springBootTests.forEach(groupRepo::delete);
    }

    @Test
    @Transactional
    void updateOneGroupPermissionByAdmin() throws Exception {
        List<PermissionResponse> allPermissions = getAllPermissions();
        List<Action> allActions = getAllActions();
        PermissionResponse manageGroupsPermission = allPermissions.stream()
                .filter(p -> p.name().equals("MANAGE GROUPS"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        Action getAction = allActions.stream()
                .filter(a -> a.getName().equals(HttpAction.GET))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Action not found"));
        PermissionWithActionsRequest addPermissionToGroupRequest = PermissionWithActionsRequest.builder()
                .permissionUuid(manageGroupsPermission.uuid())
                .actions(List.of(getAction.getUuid()))
                .build();
        String requestBody = objectMapper.writeValueAsString(List.of(addPermissionToGroupRequest));
        mockMvc.perform(patch("/group-permissions/" + groupBeforeTest.getUuid())
                        .with(opaqueToken()
                                .attributes(objectMap -> objectMap.put("email", admin.getEmail())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    void getRequestSuccessfullyPostFailsByMember() throws Exception {
        User userInGroup = saveUser("test@gmail.com");
        addUserInGroup(groupBeforeTest.getUuid(), userInGroup.getUuid());
        updateGroupPermission(groupBeforeTest.getUuid(), "MANAGE GROUPS", List.of(HttpAction.GET));

        mockMvc.perform(get("/groups")
                        .with(opaqueToken()
                                .attributes(objectMap -> objectMap.put("email", userInGroup.getEmail())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        String postRequestCreateGroup = objectMapper.writeValueAsString(GroupRequest.builder()
                .name("Failed Group")
                .build());
        mockMvc.perform(post("/groups")
                        .with(opaqueToken()
                                .attributes(objectMap -> objectMap.put("email", userInGroup.getEmail())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postRequestCreateGroup))
                .andExpect(status().isForbidden());
    }

//    @Test
//    @Transactional
//    void getRequestSuccessfullyPermissionFromAnotherGroup() throws Exception {
//        AppUser userInGroup = saveUser("test@gmail.com");
//        GroupSimplifiedResponse childGroup = postRequestCreateGroup("Child Group", groupBeforeTest.getUuid(), false);
//        addUserInGroup(groupBeforeTest.getUuid(), userInGroup.getUuid());
//        updateGroupPermission(groupBeforeTest.getUuid(), "MANAGE GROUPS", List.of(HttpAction.GET));
//        updateGroupPermission(childGroup.uuid(), "MANAGE GROUPS", List.of(HttpAction.GET));
//
//        mockMvc.perform(get("/group-list")
//                        .with(opaqueToken()
//                                .attributes(objectMap -> objectMap.put("email", userInGroup.getEmail())))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//
//        // clear groupBeforeTest permissions
//        updateGroupPermission(groupBeforeTest.getUuid(), "MANAGE GROUPS", List.of());
//        // get request by user should be ok
//        ResultActions getParentGroupByUuidResult = mockMvc.perform(get("/group-list/" + groupBeforeTest.getUuid())
//                        .with(opaqueToken()
//                                .attributes(objectMap -> objectMap.put("email", userInGroup.getEmail())))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//        GroupSimplifiedResponse groupBeforeTestUpdated = objectMapper.readValue(
//                getContentFromResponse(getParentGroupByUuidResult),
//                new TypeReference<>() {
//                });
//        // groupBeforeTest should have permission with actions empty, but the user should still get the group because of the child group permissions
//        assertEquals(1, groupBeforeTestUpdated.permissions().size());
//        PermissionWithActionsResponse permissionWithActionsResponse = groupBeforeTestUpdated.permissions()
//                .stream()
//                .findFirst()
//                .orElseThrow();
//        assertTrue(permissionWithActionsResponse.actions().isEmpty());
//
//        // childGroup should have one permission with one action
//        ResultActions childGroupByUuidResult = mockMvc.perform(get("/group-list/" + childGroup.uuid())
//                        .with(opaqueToken()
//                                .attributes(objectMap -> objectMap.put("email", userInGroup.getEmail())))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//        GroupSimplifiedResponse childGroupUpdated = objectMapper.readValue(
//                getContentFromResponse(childGroupByUuidResult),
//                new TypeReference<>() {
//                });
//        assertEquals(1, childGroupUpdated.permissions().size());
//        assertEquals(1, childGroupUpdated.permissions().get(0).actions().size());
//        assertEquals(HttpAction.GET, childGroupUpdated.permissions().get(0).actions().get(0).name());
//    }
//
//    @Test
//    @Transactional
//    void inheritPermissionsFromParentGroup() throws Exception {
//        GroupSimplifiedResponse rootGroup = postRequestCreateGroup("Root Group", null, false);
//        updateGroupPermission(rootGroup.uuid(), "MANAGE GROUPS", List.of(HttpAction.GET, HttpAction.POST, HttpAction.PATCH, HttpAction.DELETE));
//        GroupSimplifiedResponse childGroup = postRequestCreateGroup("Child Group", rootGroup.uuid(), true);
//        ResultActions childGroupByUuidResult = mockMvc.perform(get("/group-list/" + childGroup.uuid())
//                        .with(opaqueToken()
//                                .attributes(objectMap -> objectMap.put("email", superAdmin.getEmail())))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//        GroupSimplifiedResponse childGroupFromResponse = objectMapper.readValue(
//                getContentFromResponse(childGroupByUuidResult),
//                new TypeReference<>() {
//                });
//        assertEquals(1, childGroupFromResponse.permissions().size());
//        PermissionWithActionsResponse permission = childGroupFromResponse.permissions().stream()
//                .findFirst()
//                .orElseThrow();
//        assertEquals("MANAGE GROUPS",permission.name());
//        assertEquals(4, permission.actions().size());
//        permission.actions().stream()
//                .map(ActionResponse::name)
//                .forEach(a -> assertTrue(List.of(HttpAction.GET, HttpAction.POST, HttpAction.PATCH, HttpAction.DELETE).contains(a)));
//    }
//
//    @Test
//    @Transactional
//    void updateInheritPermissionsFromParentGroup() throws Exception {
//        GroupSimplifiedResponse rootGroup = postRequestCreateGroup("Root Group", null, false);
//        updateGroupPermission(rootGroup.uuid(), "MANAGE GROUPS", List.of(HttpAction.GET, HttpAction.POST, HttpAction.PATCH, HttpAction.DELETE));
//        GroupSimplifiedResponse childGroup = postRequestCreateGroup("Child Group", rootGroup.uuid(), false);
//
//        GroupSimplifiedResponse childGroupFromResponse = getGroupByUuid(childGroup.uuid());
//        assertEquals(0, childGroupFromResponse.permissions().size());
//
//        updateGroupPermission(childGroupFromResponse.uuid(), "MANAGE GROUPS-USERS", List.of(HttpAction.GET));
//        updateGroupPermission(childGroupFromResponse.uuid(), "MANAGE INTERVIEWS", List.of(HttpAction.GET));
//        GroupSimplifiedResponse childGroupWithUpdatedPermissions = getGroupByUuid(childGroup.uuid());
//        assertEquals(2, childGroupWithUpdatedPermissions.permissions().size());
//
//        GroupRequest request = GroupRequest.builder()
//                .inheritPermissions(true)
//                .build();
//        // update the child group to inherit permissions
//        ResultActions resultActionsFromUpdate = mockMvc.perform(patch("/group-list/{uuid}", childGroup.uuid())
//                        .with(opaqueToken()
//                                .attributes(objectMap -> objectMap.put("email", superAdmin.getEmail())))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk());
//        GroupSimplifiedResponse childGroupUpdatedToInheritPermissionsFromParent = objectMapper.readValue(
//                getContentFromResponse(resultActionsFromUpdate),
//                new TypeReference<>() {
//                });
//        assertEquals(1, childGroupUpdatedToInheritPermissionsFromParent.permissions().size());
//        PermissionWithActionsResponse permission = childGroupUpdatedToInheritPermissionsFromParent.permissions().stream()
//                .findFirst()
//                .orElseThrow();
//        assertEquals("MANAGE GROUPS", permission.name());
//        assertEquals(4, permission.actions().size());
//        permission.actions().stream()
//                .map(ActionResponse::name)
//                .forEach(a -> assertTrue(List.of(HttpAction.GET, HttpAction.POST, HttpAction.PATCH, HttpAction.DELETE).contains(a)));
//    }
//
//    @Test
//    @Transactional
//    void updateParentPermissionsHierarchyTest() throws Exception {
//        // create the root group with 2 permissions
//        GroupSimplifiedResponse rootGroup = postRequestCreateGroup("Root Group", null, null);
//        updateGroupPermission(rootGroup.uuid(), "MANAGE GROUPS", List.of(HttpAction.GET, HttpAction.POST, HttpAction.PATCH, HttpAction.DELETE));
//        updateGroupPermission(rootGroup.uuid(), "MANAGE GROUPS-USERS", List.of(HttpAction.GET, HttpAction.POST, HttpAction.PATCH, HttpAction.DELETE));
//
//        // create the child group with inherit_permissions FALSE and 0 permissions
//        GroupSimplifiedResponse childFirstLevelNotInherit = postRequestCreateGroup("Child first level", rootGroup.uuid(), false);
//        assertTrue(childFirstLevelNotInherit.permissions().isEmpty());
//
//        // add one permission to the child level 1 group
//        updateGroupPermission(childFirstLevelNotInherit.uuid(), "MANAGE USERS", List.of(HttpAction.GET, HttpAction.POST, HttpAction.PATCH, HttpAction.DELETE));
//        assertEquals(1, getGroupByUuid(childFirstLevelNotInherit.uuid()).permissions().size());
//
//        // Create the child level 2 group with inherit_permissions TRUE.
//        // Should inherit the permissions from the immediate parent group
//        GroupSimplifiedResponse childSecondLevelInherit = postRequestCreateGroup("Child second level", childFirstLevelNotInherit.uuid(), true);
//        assertEquals(1, getGroupByUuid(childSecondLevelInherit.uuid()).permissions().size());
//
//        // Create the child level 3 group with inherit_permissions TRUE.
//        // Should inherit the permissions from the immediate parent group
//        GroupSimplifiedResponse childThirdLevelInherit = postRequestCreateGroup("Child third level", childSecondLevelInherit.uuid(), true);
//        assertEquals(1, childThirdLevelInherit.permissions().size());
//
//        // Create the child level 4 group with inherit_permissions FALSE.
//        // Should inherit the permissions from the immediate parent group
//        GroupSimplifiedResponse childForthLevelNotInherit = postRequestCreateGroup("Child forth level", childThirdLevelInherit.uuid(), false);
//        updateGroupPermission(childForthLevelNotInherit.uuid(), "MANAGE TEMPLATES", List.of(HttpAction.GET, HttpAction.POST, HttpAction.PATCH, HttpAction.DELETE));
//        assertEquals(1, getGroupByUuid(childForthLevelNotInherit.uuid()).permissions().size());
//
//        // clear actions from one permission from the root group
//        // the child groups with inherit_permissions TRUE should update their permissions
//        updateGroupPermission(rootGroup.uuid(), "MANAGE GROUPS", List.of());
//        GroupSimplifiedResponse childFirstLevelInheritFalse = getGroupByUuid(childFirstLevelNotInherit.uuid());
//
//
//        // check actions for the child groups after update
//        getGroupByUuid(rootGroup.uuid()).permissions().stream()
//                .filter(p -> p.name().equals("MANAGE GROUPS"))
//                .findFirst()
//                .ifPresent(p -> assertTrue(p.actions().isEmpty()));
//        assertEquals(1, getGroupByUuid(childFirstLevelInheritFalse.uuid()).permissions().size());
//
//        // add one permission to childFirstLevelInheritFalse
//        updateGroupPermission(childFirstLevelInheritFalse.uuid(), "MANAGE GROUPS", List.of(HttpAction.GET, HttpAction.POST, HttpAction.PATCH, HttpAction.DELETE));
//
//        // now the children should have MANAGE GROUPS with 4 actions
//        GroupSimplifiedResponse childSecondLevelInheritTrue = getGroupByUuid(childSecondLevelInherit.uuid());
//        GroupSimplifiedResponse childThirdLevelInheritTrue = getGroupByUuid(childThirdLevelInherit.uuid());
//        GroupSimplifiedResponse childForthLevelInheritFalse = getGroupByUuid(childForthLevelNotInherit.uuid());
//
//        //check 1
//        assertEquals(2, childSecondLevelInheritTrue.permissions().size());
//        childSecondLevelInheritTrue.permissions().stream()
//                .filter(p -> p.name().equals("MANAGE GROUPS"))
//                .findFirst()
//                .ifPresentOrElse(
//                        p -> assertEquals(4, p.actions().size()),
//                        () -> {
//                            throw new RuntimeException("Permission not found");
//                        });
//        //check 2
//        assertEquals(2, childThirdLevelInheritTrue.permissions().size());
//        childThirdLevelInheritTrue.permissions().stream()
//                .filter(p -> p.name().equals("MANAGE GROUPS"))
//                .findFirst()
//                .ifPresentOrElse(
//                        p -> assertEquals(4, p.actions().size()),
//                        () -> {
//                            throw new RuntimeException("Permission not found");
//                        });
//        //check 3
//        // the forth level should not update permissions because it has inherit_permissions FALSE and update method stops at the first group with inherit_permissions FALSE
//        assertEquals(1, childForthLevelInheritFalse.permissions().size());
//        childForthLevelInheritFalse.permissions().stream()
//                .map(PermissionWithActionsResponse::name)
//                .findFirst()
//                .ifPresentOrElse(
//                        p -> assertEquals("MANAGE TEMPLATES", p),
//                        () -> {
//                            throw new RuntimeException("Permission not found");
//                        });
//
//        // now the childFirstLevelNotInherit will update its field inheritPermissions to TRUE
//        // So it should inherit the permissions from the root group
//        // and distribute them to the children until a group with inheritPermissions FALSE is found
//        GroupRequest request = GroupRequest.builder()
//                .inheritPermissions(true)
//                .build();
//        mockMvc.perform(patch("/group-list/{uuid}", childFirstLevelInheritFalse.uuid())
//                .with(opaqueToken()
//                        .attributes(objectMap -> objectMap.put("email", superAdmin.getEmail())))
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)));
//
//        //checks
//        GroupSimplifiedResponse childFirstLevelInheritTrue = getGroupByUuid(childFirstLevelInheritFalse.uuid());
//        GroupSimplifiedResponse gchildSecondLevelInheritTrue = getGroupByUuid(childSecondLevelInheritTrue.uuid());
//        GroupSimplifiedResponse gchildThirdLevelInheritTrue = getGroupByUuid(childThirdLevelInheritTrue.uuid());
//        GroupSimplifiedResponse gchildForthLevelInheritFalse = getGroupByUuid(childForthLevelInheritFalse.uuid());
//        assertEquals(2, childFirstLevelInheritTrue.permissions().size());
//        assertEquals(2, gchildSecondLevelInheritTrue.permissions().size());
//        assertEquals(2, gchildThirdLevelInheritTrue.permissions().size());
//        assertEquals(1, gchildForthLevelInheritFalse.permissions().size());
//
//
//        // childFirstLevelInherit go again to inheritPermissions FALSE,
//        // this means that its permissions will be cleared
//        // and the children will also have their permissions cleared
//        // until a group with inheritPermissions FALSE is found (child level 4)
//        GroupRequest requestFalse = GroupRequest.builder()
//                .inheritPermissions(false)
//                .build();
//        mockMvc.perform(patch("/group-list/{uuid}", childFirstLevelInheritFalse.uuid())
//                .with(opaqueToken()
//                        .attributes(objectMap -> objectMap.put("email", superAdmin.getEmail())))
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(requestFalse)));
//        GroupSimplifiedResponse gchildFirstLevelInheritFalse = getGroupByUuid(childFirstLevelInheritTrue.uuid());
//        GroupSimplifiedResponse ggchildSecondLevelInheritTrue = getGroupByUuid(gchildSecondLevelInheritTrue.uuid());
//        GroupSimplifiedResponse ggchildThirdLevelInheritTrue = getGroupByUuid(gchildThirdLevelInheritTrue.uuid());
//        GroupSimplifiedResponse ggchildForthLevelInheritFalse = getGroupByUuid(gchildForthLevelInheritFalse.uuid());
//
//        assertEquals(0, gchildFirstLevelInheritFalse.permissions().size());
//        assertEquals(0, ggchildSecondLevelInheritTrue.permissions().size());
//        assertEquals(0, ggchildThirdLevelInheritTrue.permissions().size());
//        assertEquals(1, ggchildForthLevelInheritFalse.permissions().size());
//    }
//
private User saveUser(String email) {
    return appUserService.save(User.builder()
            .name("Name")
            .surname("Surname")
            .email(UUID.randomUUID().toString().substring(0, 5) + email)
            .groups(new HashSet<>())
            .build(), "MEMBER");
}

    private void addUserInGroup(UUID groupUuid, UUID userUuid) throws Exception {
        List<UserGroupRequestMap> userGroupMapList = List.of(UserGroupRequestMap.builder()
                .userUuid(userUuid)
                .groupUuid(groupUuid)
                .build());

        String requestBody = objectMapper.writeValueAsString(userGroupMapList);

        mockMvc.perform(post("/group-users")
                        .with(opaqueToken()
                                .attributes(objectMap -> objectMap.put("email", admin.getEmail())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }


    private void updateGroupPermission(UUID groupUuid, String permissionName, List<HttpAction> requestActions) throws Exception {
        List<PermissionResponse> allPermissions = getAllPermissions();
        List<Action> allActions = getAllActions();
        PermissionResponse permissionToAdd = allPermissions.stream()
                .filter(p -> p.name().equals(permissionName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        List<Action> actionsToAdd = allActions.stream()
                .filter(a -> requestActions.contains(a.getName()))
                .toList();
        PermissionWithActionsRequest addPermissionToGroupRequest = PermissionWithActionsRequest.builder()
                .permissionUuid(permissionToAdd.uuid())
                .actions(actionsToAdd.stream().map(Action::getUuid).toList())
                .build();
        String requestBody = objectMapper.writeValueAsString(List.of(addPermissionToGroupRequest));
        mockMvc.perform(patch("/group-permissions/" + groupUuid)
                        .with(opaqueToken()
                                .attributes(objectMap -> objectMap.put("email", admin.getEmail())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }
//
//    private GroupSimplifiedResponse postRequestCreateGroup(String name, UUID parentUuid, Boolean inheritPermissions) throws Exception {
//        GroupRequest request = GroupRequest.builder()
//                .name(String.format("Spring Boot Test %s %s", name, UUID.randomUUID().toString().substring(0, 5)))
//                .parentUuid(parentUuid)
//                .inheritPermissions(inheritPermissions)
//                .build();
//
//        String requestBody = objectMapper.writeValueAsString(request);
//
//        ResultActions resultActions = mockMvc.perform(post("/group-list")
//                        .with(opaqueToken()
//                                .attributes(objectMap -> objectMap.put("email", superAdmin.getEmail())))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isOk());
//        return objectMapper.readValue(
//                getContentFromResponse(resultActions),
//                new TypeReference<>() {
//                });
//    }
//
private List<PermissionResponse> getAllPermissions() {
    return permissionService.getAll();

}

    private List<Action> getAllActions() throws Exception {
        return actionRepo.findAll();

    }
//
//    private static String getContentFromResponse(ResultActions resultActions) throws Exception {
//        MvcResult mvcResult = resultActions.andReturn();
//        return mvcResult.getResponse().getContentAsString();
//    }
//
//    private GroupSimplifiedResponse getGroupByUuid(UUID groupUuid) throws Exception {
//        ResultActions childGroupByUuidResult = mockMvc.perform(get("/group-list/" + groupUuid)
//                        .with(opaqueToken()
//                                .attributes(objectMap -> objectMap.put("email", superAdmin.getEmail())))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//        return objectMapper.readValue(
//                getContentFromResponse(childGroupByUuidResult),
//                new TypeReference<>() {
//                });
//    }
//
private void cleanSession() {
    entityManager.flush();
    entityManager.clear();
}
}
