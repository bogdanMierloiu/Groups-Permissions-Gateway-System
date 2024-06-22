package com.bogdan_mierloiu.permissions_system.repo;


import com.bogdan_mierloiu.permissions_system.entity.Group;
import jakarta.persistence.EntityManager;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration
@WebAppConfiguration
@ActiveProfiles("test")
class GroupRepoTest {

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:16.3")
            .withDatabaseName("permissions_system")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    private GroupRepo groupRepo;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void save() {
        Group rootGroupBuild = Group.builder()
                .name("Group Test" + randomString())
                .description("Group Test Description")
                .parent(null)
                .enabled(true)
                .inheritPermissions(false)
                .build();
        Group rootGroupSaved = groupRepo.save(rootGroupBuild);
        assertEquals(rootGroupSaved.getName(), rootGroupBuild.getName());
    }

    @Test
    @Transactional
    void findByUuid() {
        Group rootGroupBuild = Group.builder()
                .name("Group Test" + randomString())
                .description("Group Test Description")
                .parent(null)
                .enabled(true)
                .inheritPermissions(false)
                .build();
        Group rootGroupSaved = groupRepo.save(rootGroupBuild);
        Optional<Group> rootGroupFound = groupRepo.findByUuid(rootGroupSaved.getUuid());
        assertTrue(rootGroupFound.isPresent());
        assertEquals(rootGroupFound.get().getName(), rootGroupSaved.getName());
    }

    @Test
    @Transactional
    void update() {
        Group rootGroupBuild = Group.builder()
                .name("Group Test" + randomString())
                .description("Group Test Description")
                .parent(null)
                .enabled(true)
                .inheritPermissions(false)
                .build();
        Group rootGroupSaved = groupRepo.save(rootGroupBuild);

        rootGroupSaved.setName("Group Test Updated");
        rootGroupSaved.setDescription("Group Test Description Updated");
        rootGroupSaved.setEnabled(false);
        rootGroupSaved.setInheritPermissions(true);

        Group rootGroupUpdatedName = groupRepo.save(rootGroupSaved);

        assertEquals(rootGroupUpdatedName.getName(), rootGroupSaved.getName());
        assertEquals(rootGroupUpdatedName.getDescription(), rootGroupSaved.getDescription());
        assertEquals(rootGroupUpdatedName.getEnabled(), rootGroupSaved.getEnabled());
        assertEquals(rootGroupUpdatedName.getInheritPermissions(), rootGroupSaved.getInheritPermissions());

        Group childGroup = Group.builder()
                .name("Child Group Test" + randomString())
                .description("Child Group Test Description")
                .parent(rootGroupUpdatedName)
                .enabled(true)
                .inheritPermissions(false)
                .build();

        Group childGroupSaved = groupRepo.save(childGroup);
        assertEquals(childGroupSaved.getParent().getName(), rootGroupUpdatedName.getName());

        childGroupSaved.setParent(null);
        Group childGroupUpdated = groupRepo.save(childGroupSaved);
        assertNull(childGroupUpdated.getParent());

        Group secondRootGroup = Group.builder()
                .name("Second Root Group Test" + randomString())
                .description("Second Root Group Test Description")
                .parent(null)
                .enabled(true)
                .inheritPermissions(false)
                .build();
        Group secondRootGroupSaved = groupRepo.save(secondRootGroup);

        childGroupSaved.setParent(rootGroupSaved);
        Group childGroupUpdatedParent = groupRepo.save(childGroupSaved);
        assertEquals(childGroupUpdatedParent.getParent().getName(), rootGroupUpdatedName.getName());

        childGroupUpdatedParent.setParent(secondRootGroupSaved);
        Group childGroupUpdatedParentUpdated = groupRepo.save(childGroupUpdatedParent);
        assertEquals(childGroupUpdatedParentUpdated.getParent().getName(), secondRootGroupSaved.getName());

    }

    @Test
    @Transactional
    void delete() {
        Group rootGroupRequest = Group.builder()
                .name("Group Test" + randomString())
                .description("Group Test Description")
                .parent(null)
                .enabled(true)
                .inheritPermissions(false)
                .build();
        Group rootGroupSaved = groupRepo.save(rootGroupRequest);
        groupRepo.delete(rootGroupSaved);
        Optional<Group> rootGroupFound = groupRepo.findByUuid(rootGroupSaved.getUuid());
        assertFalse(rootGroupFound.isPresent());


        Group rootHierarchyGroup = Group.builder()
                .name("Root Hierarchy Group Test" + randomString())
                .description("Root Hierarchy Group Test Description")
                .parent(null)
                .enabled(true)
                .inheritPermissions(false)
                .build();

        Group rootHierarchyGroupSaved = groupRepo.save(rootHierarchyGroup);

        Group childGroup = Group.builder()
                .name("Child Group Test" + randomString())
                .description("Child Group Test Description")
                .parent(rootHierarchyGroupSaved)
                .enabled(true)
                .inheritPermissions(false)
                .build();

        Group childGroupSaved = groupRepo.save(childGroup);

        Group grandChildGroup = Group.builder()
                .name("Grand Child Group Test" + randomString())
                .description("Grand Child Group Test Description")
                .parent(childGroupSaved)
                .enabled(true)
                .inheritPermissions(false)
                .build();
        Group grandChildGroupSaved = groupRepo.save(grandChildGroup);

        Group anotherRootGroup = Group.builder()
                .name("Second Root Group Test" + randomString())
                .description(" Test Description")
                .parent(null)
                .enabled(true)
                .inheritPermissions(false)
                .build();
        groupRepo.save(anotherRootGroup);

        groupRepo.delete(rootHierarchyGroupSaved);
        cleanSession();

        Optional<Group> rootHierarchyGroupFound = groupRepo.findByUuid(rootHierarchyGroupSaved.getUuid());
        Optional<Group> childGroupFound = groupRepo.findByUuid(childGroupSaved.getUuid());
        Optional<Group> grandChildGroupFound = groupRepo.findByUuid(grandChildGroupSaved.getUuid());

        assertFalse(rootHierarchyGroupFound.isPresent());
        assertFalse(childGroupFound.isPresent());
        assertFalse(grandChildGroupFound.isPresent());

        List<Group> groups = groupRepo.findAll();
        assertEquals(1, groups.size());
        groups.stream().findFirst().ifPresent(group -> assertEquals(anotherRootGroup.getName(), group.getName()));

    }

    private String randomString() {
        return UUID.randomUUID().toString().substring(0, 5);
    }

    private void cleanSession() {
        entityManager.flush();
        entityManager.clear();
    }

}