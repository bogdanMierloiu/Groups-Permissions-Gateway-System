package com.bogdan_mierloiu.permissions_system.repo;

import com.bogdan_mierloiu.permissions_system.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface GroupRepo extends JpaRepository<Group, Long> {

    Optional<Group> findByUuid(UUID uuid);

    @Query("SELECT g FROM Group g WHERE g.parent IS NULL AND g.name = :name")
    Set<Group> findRootGroupsByName(String name);

    @Query("SELECT g FROM Group g WHERE g.name LIKE %:name%")
    List<Group> findByNameContains(@Param("name") String name);

    @Query("SELECT g FROM Group g WHERE g.parent IS NULL")
    List<Group> findRootGroups();
}
