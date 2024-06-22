package com.bogdan_mierloiu.permissions_system.service;

import com.bogdan_mierloiu.permissions_system.dto.permission.PermissionResponse;
import com.bogdan_mierloiu.permissions_system.mapper.PermissionMapper;
import com.bogdan_mierloiu.permissions_system.repo.GroupPermissionRepo;
import com.bogdan_mierloiu.permissions_system.repo.PermissionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private static final String PERMISSION = "Permission";

    private final PermissionRepo permissionRepo;
    private final GroupPermissionRepo groupPermissionRepo;

    public Optional<PermissionResponse> getByUuid(UUID uuid) {
        return permissionRepo.findByUuid(uuid)
                .map(PermissionMapper::entityToDto);
    }

    public List<PermissionResponse> getAll() {
        return PermissionMapper.entityListToDto(permissionRepo.findAll());
    }

}
