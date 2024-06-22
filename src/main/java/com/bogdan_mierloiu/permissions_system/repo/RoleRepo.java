package com.bogdan_mierloiu.permissions_system.repo;

import com.bogdan_mierloiu.permissions_system.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepo extends JpaRepository<Role, Long> {

    Role findByName(String name);
}
