package com.bogdan_mierloiu.permissions_system.repo;


import com.bogdan_mierloiu.permissions_system.entity.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActionRepo extends JpaRepository<Action, Long> {

    @Query("SELECT a FROM Action a WHERE a.uuid = :uuid")
    Optional<Action> findByUuid(UUID uuid);

}
