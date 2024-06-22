package com.bogdan_mierloiu.permissions_system.service;


import com.bogdan_mierloiu.permissions_system.dto.group.GroupRequest;
import com.bogdan_mierloiu.permissions_system.dto.group.GroupSimplifiedResponse;
import com.bogdan_mierloiu.permissions_system.entity.Group;
import com.bogdan_mierloiu.permissions_system.repo.GroupRepo;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
@WebAppConfiguration
@ActiveProfiles("test")
class GroupServiceTest {

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:16.3")
            .withDatabaseName("permissions_system")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    private GroupService groupService;

    @MockBean
    private GroupRepo groupRepo;

    @Test
    @Transactional
    void save() {
        Group rootGroup = Group.builder()
                .name("root group")
                .description("description")
                .parent(null)
                .enabled(true)
                .inheritPermissions(false)
                .children(Set.of())
                .users(Set.of())
                .uuid(UUID.randomUUID())
                .build();

        Group childGroup = Group.builder()
                .name("Child group")
                .description("description")
                .parent(rootGroup)
                .enabled(true)
                .inheritPermissions(false)
                .children(Set.of())
                .users(Set.of())
                .uuid(UUID.randomUUID())
                .build();

        GroupRequest groupRequest = GroupRequest.builder()
                .name("Child group")
                .description("description")
                .parentUuid(rootGroup.getUuid())
                .enabled(true)
                .inheritPermissions(false)
                .build();

        when(groupRepo.findByUuid(rootGroup.getUuid())).thenReturn(Optional.of(rootGroup));
        when(groupRepo.findRootGroupsByName(groupRequest.name())).thenReturn(Set.of());
        when(groupRepo.save(any(Group.class))).thenReturn(childGroup);

        GroupSimplifiedResponse groupSimplifiedResponse = groupService.save(groupRequest);

        assertEquals(groupSimplifiedResponse.name(), groupRequest.name());
        assertEquals(groupSimplifiedResponse.description(), groupRequest.description());
        assertEquals(groupSimplifiedResponse.parentUuid(), groupRequest.parentUuid());
        assertEquals(groupSimplifiedResponse.enabled(), groupRequest.enabled());
        assertEquals(groupSimplifiedResponse.inheritPermissions(), groupRequest.inheritPermissions());
    }
}
