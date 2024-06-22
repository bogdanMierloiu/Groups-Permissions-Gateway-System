package com.bogdan_mierloiu.permissions_system.controller;

import com.bogdan_mierloiu.permissions_system.config.ApplicationContextProvider;
import com.bogdan_mierloiu.permissions_system.dto.group.GroupRequest;
import com.bogdan_mierloiu.permissions_system.dto.group.GroupSimplifiedResponse;
import com.bogdan_mierloiu.permissions_system.dto.group.UserGroupRequestMap;
import com.bogdan_mierloiu.permissions_system.entity.Group;
import com.bogdan_mierloiu.permissions_system.entity.User;
import com.bogdan_mierloiu.permissions_system.repo.GroupRepo;
import com.bogdan_mierloiu.permissions_system.repo.UserRepo;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
@WebAppConfiguration
@ActiveProfiles("test")
class GroupControllerTest {

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:16.3")
            .withDatabaseName("interview_feedback")
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
    private UserRepo userRepo;

    @Autowired
    private UserService userService;

    private User admin;
    private User member;
    private Group groupBeforeTest;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        Optional<User> optionalAdmin = userRepo.findByEmail("java-admin-test@gmail.com");
        Optional<User> optionalMember = userRepo.findByEmail("java-member-test@gmail.com");

        member = optionalMember.orElseGet(() -> userService.save(User.builder()
                .name("Test")
                .surname("Member")
                .email("java-member-test@gmail.com")
                .groups(new HashSet<>())
                        .build(),
                "MEMBER"));

        admin = optionalAdmin.orElseGet(() -> userService.save(User.builder()
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

    @Test
    @Transactional
    void getAllRootGroupsByAdmin() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/groups")
                        .with(opaqueToken()
                                .attributes(objectMap -> objectMap.put("email", admin.getEmail()))))
                .andExpect(status().isOk());

        List<GroupSimplifiedResponse> groupSimplifiedRespons = objectMapper.readValue(
                getContentFromResponse(resultActions),
                new TypeReference<>() {
                });
        groupSimplifiedRespons.forEach(
                g -> assertNull(g.parentUuid()));
    }


    @Test
    @Transactional
    void getAllRootGroupsByMemberFails() throws Exception {
        mockMvc.perform(get("/group-list")
                        .with(opaqueToken()
                                .attributes(objectMap -> objectMap.put("email", member.getEmail()))))
                .andExpect(status().is4xxClientError());
    }


    @Test
    @Transactional
    void getGroupByUuidBySuperAdmin() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/groups/{uuid}", groupBeforeTest.getUuid())
                        .with(opaqueToken()
                                .authorities(List.of(new SimpleGrantedAuthority("SUPER_ADMIN")))
                                .attributes(objectMap -> objectMap.put("email", admin.getEmail()))))
                .andExpect(status().isOk());
        GroupSimplifiedResponse groupSimplifiedResponse = objectMapper.readValue(
                getContentFromResponse(resultActions),
                new TypeReference<>() {
                });
        assertEquals(groupBeforeTest.getName(), groupSimplifiedResponse.name());
    }


    @Test
    @Transactional
    void saveGroupsBySuperAdmin() throws Exception {
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

        // Create a child group
        GroupSimplifiedResponse childGroup = getGroupResponseFromResultActions(
                postRequestCreateGroup("Child", rootGroup.uuid()));

        groupRepo.findByUuid(childGroup.uuid())
                .ifPresent(g -> assertEquals(1, g.getUsers().size()));
    }

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




