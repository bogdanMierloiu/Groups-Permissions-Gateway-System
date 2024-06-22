package com.bogdan_mierloiu.permissions_system.controller;

import com.bogdan_mierloiu.permissions_system.config.ApplicationContextProvider;
import com.bogdan_mierloiu.permissions_system.dto.group.GroupRequest;
import com.bogdan_mierloiu.permissions_system.dto.group.GroupSimplifiedResponse;
import com.bogdan_mierloiu.permissions_system.dto.group.UserGroupRequestMap;
import com.bogdan_mierloiu.permissions_system.entity.Group;
import com.bogdan_mierloiu.permissions_system.entity.User;
import com.bogdan_mierloiu.permissions_system.repo.GroupRepo;
import com.bogdan_mierloiu.permissions_system.repo.UserRepo;
import com.bogdan_mierloiu.permissions_system.service.GroupService;
import com.bogdan_mierloiu.permissions_system.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
@WebAppConfiguration
@ActiveProfiles("test")
class GroupUsersControllerTest {

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:16.3")
            .withDatabaseName("permissions_system")
            .withUsername("postgres")
            .withPassword("postgres");


    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GroupRepo groupRepo;

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserService appUserService;

    private User member;
    private User admin;
    private Group groupBeforeTest;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        Optional<User> optionalAdmin = userRepo.findByEmail("java-admin-test@gmail.com");
        Optional<User> optionalMember = userRepo.findByEmail("java-member-test@gmail.com");

        member = optionalMember.orElseGet(() -> appUserService.save(User.builder()
                        .name("Test")
                        .surname("Member")
                        .email("java-member-test@gmail.com")
                        .groups(new HashSet<>())
                        .build(),
                "MEMBER"));

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


    //
//    @Test
//    @Transactional
//    void addUOneUserToAGroupByAdminSuccessfully() throws Exception {
//        List<UserGroupMap> userGroupMapList = List.of(UserGroupMap.builder()
//                .userUuid(superAdmin.getUuid())
//                .groupUuid(groupBeforeTest.getUuid())
//                .build());
//
//        String requestBody = objectMapper.writeValueAsString(userGroupMapList);
//
//        mockMvc.perform(post("/group-users")
//                        .with(opaqueToken()
//                                .attributes(objectMap -> objectMap.put("email", superAdmin.getEmail())))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isOk());
//    }
//
//
//    @Test
//    @Transactional
//    void addUOneUserToGroupsHierarchyByAdminSuccessfully() throws Exception {
//        Group parentRequest = Group.builder()
//                .name("Spring Boot Test Root Group " + UUID.randomUUID())
//                .users(new HashSet<>())
//                .inheritPermissions(false)
//                .uuid(UUID.randomUUID())
//                .enabled(true)
//                .build();
//        Group parent = groupRepo.saveAndFlush(parentRequest);
//
//        Group childLevel1Request = Group.builder()
//                .name("Spring Boot Test Child Group " + UUID.randomUUID())
//                .parent(parent)
//                .users(new HashSet<>())
//                .inheritPermissions(false)
//                .uuid(UUID.randomUUID())
//                .enabled(true)
//                .build();
//        Group childLevel1 = groupRepo.saveAndFlush(childLevel1Request);
//
//        Group childLevel2Request = Group.builder()
//                .name("Spring Boot Test Child Group " + UUID.randomUUID())
//                .parent(childLevel1)
//                .users(new HashSet<>())
//                .uuid(UUID.randomUUID())
//                .inheritPermissions(false)
//                .enabled(true)
//                .build();
//        Group childLevel2 = groupRepo.saveAndFlush(childLevel2Request);
//
//        // add user to a parent group
//        List<UserGroupMap> userGroupMapList = List.of(UserGroupMap.builder()
//                .userUuid(memberUser.getUuid())
//                .groupUuid(parent.getUuid())
//                .build());
//
//        String requestBody = objectMapper.writeValueAsString(userGroupMapList);
//
//        ResultActions resultActions = mockMvc.perform(post("/group-users")
//                        .with(opaqueToken()
//                                .attributes(objectMap -> objectMap.put("email", superAdmin.getEmail())))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isOk());
//        List<AppUserSimplifiedResponse> usersAddedResponse = objectMapper.readValue(
//                getContentFromResponse(resultActions),
//                new TypeReference<>() {
//                });
//
//        entityManager.flush();
//        entityManager.clear();
//
//        Group parentGroup = groupService.getEntityByUuid(parent.getUuid());
//        Group child1Group = groupService.getEntityByUuid(childLevel1.getUuid());
//        Group child2Group = groupService.getEntityByUuid(childLevel2.getUuid());
//
//        assertEquals(1, usersAddedResponse.size());
//        assertTrue(usersAddedResponse.stream().anyMatch(u -> u.email().equals(memberUser.getEmail())));
//
//        assertEquals(1, parentGroup.getUsers().size());
//        assertTrue(parentGroup.getUsers().stream().anyMatch(u -> u.getEmail().equals(memberUser.getEmail())));
//
//        assertEquals(1, child1Group.getUsers().size());
//        assertTrue(child1Group.getUsers().stream().anyMatch(u -> u.getEmail().equals(memberUser.getEmail())));
//
//        assertEquals(1, child2Group.getUsers().size());
//        assertTrue(child2Group.getUsers().stream().anyMatch(u -> u.getEmail().equals(memberUser.getEmail())));
//    }
//
//
//    @Test
//    void addUOneUserToAGroupNotFound() throws Exception {
//        UUID randomUUID = UUID.randomUUID();
//        List<UserGroupMap> userGroupMapList = List.of(UserGroupMap.builder()
//                .userUuid(memberUser.getUuid())
//                .groupUuid(randomUUID)    // group not found
//                .build());
//
//        String requestBody = objectMapper.writeValueAsString(userGroupMapList);
//
//        mockMvc.perform(post("/group-users")
//                        .with(opaqueToken()
//                                .attributes(objectMap -> objectMap.put("email", superAdmin.getEmail())))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @Transactional
//    void addOneExistingUserToAGroupBadRequest() throws Exception {
//        List<UserGroupMap> userGroupMapList = List.of(UserGroupMap.builder()
//                .userUuid(memberUser2.getUuid())
//                .groupUuid(groupBeforeTest.getUuid())
//                .build());
//
//        String requestBody = objectMapper.writeValueAsString(userGroupMapList);
//        mockMvc.perform(post("/group-users")
//                        .with(opaqueToken()
//                                .attributes(objectMap -> objectMap.put("email", superAdmin.getEmail())))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isOk());
//        cleanSession();
//        mockMvc.perform(post("/group-users")
//                        .with(opaqueToken()
//                                .attributes(objectMap -> objectMap.put("email", superAdmin.getEmail())))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    @Transactional
//    void searchUsersSuccessfully() throws Exception {
//        AppUser userContainsInName = appUserService.save(AppUser.builder()
//                .name("Java Name")
//                .surname("User")
//                .email("email1@gmail.com")
//                .accountEnabled(true)
//                .groups(new HashSet<>())
//                .subscriptions(new HashSet<>())
//                .interviewResults(new HashSet<>())
//                .build());
//        AppUser userContainsInSurname = appUserService.save(AppUser.builder()
//                .name("User")
//                .surname("Java Surname")
//                .email("email2@gmail.com")
//                .accountEnabled(true)
//                .groups(new HashSet<>())
//                .subscriptions(new HashSet<>())
//                .interviewResults(new HashSet<>())
//                .build());
//        cleanSession();
//        SearchUserFilter searchUserFilter = SearchUserFilter.builder()
//                .characters("java")
//                .pageNumber(1)
//                .pageSize(10)
//                .build();
//        String requestBody = objectMapper.writeValueAsString(searchUserFilter);
//
//        ResultActions resultActions = mockMvc.perform(post("/group-users/filtered-users")
//                        .with(opaqueToken()
//                                .attributes(objectMap -> objectMap.put("email", superAdmin.getEmail())))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isOk());
//
//        FilteredUserResponse response = objectMapper.readValue(
//                getContentFromResponse(resultActions),
//                new TypeReference<>() {
//                });
//        // 2 users are created in this test, and 3 users are created in the setup method
//        assertEquals(5, response.users().size());
//        response.users().forEach(u -> assertTrue(
//                u.email().toLowerCase().contains("java") ||
//                        u.name().toLowerCase().contains("java") ||
//                        u.surname().toLowerCase().contains("java")));
//        List<AppUser> usersFromDb = List.of(memberUser, superAdmin, memberUser2, userContainsInName, userContainsInSurname);
//        List<AppUserOnlyNameResponse> usersMapped = usersFromDb.stream()
//                .map(AppUserMapper::entityToOnlyNameDto)
//                .sorted(Comparator.comparing(AppUserOnlyNameResponse::email))
//                .toList();
//        assertEquals(usersMapped, response.users().stream()
//                .sorted(Comparator.comparing(AppUserOnlyNameResponse::email))
//                .toList());
//    }
//
//    @Test
//    @Transactional
//    void searchUsersWithExcludeGroupSuccessfully() throws Exception {
//        AppUser userInGroup = appUserService.save(AppUser.builder()
//                .name("Java Name")
//                .surname("User")
//                .email("email3@gmail.com")
//                .accountEnabled(true)
//                .groups(new HashSet<>())
//                .subscriptions(new HashSet<>())
//                .interviewResults(new HashSet<>())
//                .build());
//        List<UserGroupMap> userGroupMapList = List.of(UserGroupMap.builder()
//                .userUuid(userInGroup.getUuid())
//                .groupUuid(groupBeforeTest.getUuid())
//                .build());
//
//        String addUserInGroupRequest = objectMapper.writeValueAsString(userGroupMapList);
//        mockMvc.perform(post("/group-users")
//                        .with(opaqueToken()
//                                .attributes(objectMap -> objectMap.put("email", superAdmin.getEmail())))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(addUserInGroupRequest))
//                .andExpect(status().isOk());
//        cleanSession();
//        SearchUserFilter searchUserFilter = SearchUserFilter.builder()
//                .characters("java")
//                .groupUuidToExclude(groupBeforeTest.getUuid())
//                .pageNumber(1)
//                .pageSize(10)
//                .build();
//        String searchUsersRequest = objectMapper.writeValueAsString(searchUserFilter);
//
//        ResultActions resultActions = mockMvc.perform(post("/group-users/filtered-users")
//                        .with(opaqueToken()
//                                .attributes(objectMap -> objectMap.put("email", superAdmin.getEmail())))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(searchUsersRequest))
//                .andExpect(status().isOk());
//
//        FilteredUserResponse response = objectMapper.readValue(
//                getContentFromResponse(resultActions),
//                new TypeReference<>() {
//                });
//        assertEquals(3, response.users().size());
//        assertTrue(response.users().stream().noneMatch(u -> u.email().equals(userInGroup.getEmail())));
//    }
//
//
    @Test
    @Transactional
    void removeUsersFromGroupSuccessfully() throws Exception {
        GroupSimplifiedResponse rootGroup = getGroupResponseFromResultActions(
                postRequestCreateGroup("Root", null));

        // Add member to the root group
        List<UserGroupRequestMap> userGroupMapRequest = List.of(UserGroupRequestMap.builder()
                .userUuid(member.getUuid())
                .groupUuid(rootGroup.uuid())
                .build());
        String addUserRequest = objectMapper.writeValueAsString(userGroupMapRequest);
        mockMvc.perform(post("/group-users")
                        .with(opaqueToken()
                                .attributes(objectMap -> objectMap.put("email", admin.getEmail())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addUserRequest))
                .andExpect(status().isOk());
        cleanSession();
        groupRepo.findByUuid(rootGroup.uuid())
                .ifPresent(g -> assertEquals(1, g.getUsers().size()));

        mockMvc.perform(delete("/group-users")
                        .with(opaqueToken()
                                .attributes(objectMap -> objectMap.put("email", admin.getEmail())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userGroupMapRequest)))
                .andExpect(status().isOk());
    }

//
//    @Test
//    @Transactional
//    void removeUOneUserFromGroupsHierarchyByAdminSuccessfully() throws Exception {
//        Group parentRequest = Group.builder()
//                .name("Spring Boot Test Root Group " + UUID.randomUUID())
//                .users(new HashSet<>())
//                .inheritPermissions(false)
//                .uuid(UUID.randomUUID())
//                .enabled(true)
//                .build();
//        Group parent = groupRepo.saveAndFlush(parentRequest);
//
//        Group childLevel1Request = Group.builder()
//                .name("Spring Boot Test Child Group " + UUID.randomUUID())
//                .parent(parent)
//                .users(new HashSet<>())
//                .uuid(UUID.randomUUID())
//                .inheritPermissions(false)
//                .enabled(true)
//                .build();
//        Group childLevel1 = groupRepo.saveAndFlush(childLevel1Request);
//
//        Group childLevel2Request = Group.builder()
//                .name("Spring Boot Test Child Group " + UUID.randomUUID())
//                .parent(childLevel1)
//                .users(new HashSet<>())
//                .uuid(UUID.randomUUID())
//                .inheritPermissions(false)
//                .enabled(true)
//                .build();
//        Group childLevel2 = groupRepo.saveAndFlush(childLevel2Request);
//
//        // add user to a parent group
//        List<UserGroupMap> userGroupMapList = List.of(UserGroupMap.builder()
//                .userUuid(memberUser.getUuid())
//                .groupUuid(parent.getUuid())
//                .build());
//
//        String requestBody = objectMapper.writeValueAsString(userGroupMapList);
//
//        mockMvc.perform(post("/group-users")
//                        .with(opaqueToken()
//                                .attributes(objectMap -> objectMap.put("email", superAdmin.getEmail())))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isOk());
//
//        mockMvc.perform(delete("/group-users")
//                        .with(opaqueToken()
//                                .attributes(objectMap -> objectMap.put("email", superAdmin.getEmail())))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isNoContent());
//
//        cleanSession();
//        Group parentGroup = groupService.getEntityByUuid(parent.getUuid());
//        Group child1Group = groupService.getEntityByUuid(childLevel1.getUuid());
//        Group child2Group = groupService.getEntityByUuid(childLevel2.getUuid());
//
//        assertEquals(0, parentGroup.getUsers().size());
//        assertTrue(parentGroup.getUsers().stream().noneMatch(u -> u.getEmail().equals(memberUser.getEmail())));
//
//        assertEquals(0, child1Group.getUsers().size());
//        assertTrue(child1Group.getUsers().stream().noneMatch(u -> u.getEmail().equals(memberUser.getEmail())));
//
//        assertEquals(0, child2Group.getUsers().size());
//        assertTrue(child2Group.getUsers().stream().noneMatch(u -> u.getEmail().equals(memberUser.getEmail())));
//    }
//
//    private static String getContentFromResponse(ResultActions resultActions) throws Exception {
//        MvcResult mvcResult = resultActions.andReturn();
//        return mvcResult.getResponse().getContentAsString();
//    }
//
private static String getContentFromResponse(ResultActions resultActions) throws Exception {
    MvcResult mvcResult = resultActions.andReturn();
    return mvcResult.getResponse().getContentAsString();
}

    private GroupSimplifiedResponse getGroupResponseFromResultActions(ResultActions resultActions) throws Exception {
        return objectMapper.readValue(
                getContentFromResponse(resultActions),
                new TypeReference<>() {
                });
    }

    private ResultActions postRequestCreateGroup(String name, UUID parentUuid) throws Exception {
        GroupRequest request = GroupRequest.builder()
                .name(String.format("Test %s Group %s", name, UUID.randomUUID().toString().substring(0, 5)))
                .parentUuid(parentUuid)
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        return mockMvc.perform(post("/groups")
                        .with(opaqueToken()
                                .attributes(objectMap -> objectMap.put("email", admin.getEmail())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    private void cleanSession() {
        entityManager.flush();
        entityManager.clear();
    }

}

