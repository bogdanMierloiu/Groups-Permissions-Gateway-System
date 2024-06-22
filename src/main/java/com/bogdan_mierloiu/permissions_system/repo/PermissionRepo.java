package com.bogdan_mierloiu.permissions_system.repo;

import com.bogdan_mierloiu.permissions_system.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepo extends JpaRepository<Permission, Long> {

    Optional<Permission> findByUuid(UUID uuid);

}
