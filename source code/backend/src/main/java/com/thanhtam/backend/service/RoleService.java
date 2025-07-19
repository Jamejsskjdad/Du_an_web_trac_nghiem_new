package com.thanhtam.backend.service;

import java.util.Optional;

import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.ultilities.ERole;

public interface RoleService {
    Optional<Role> findByName(ERole name);
    Optional<Role> findById(Long id);
}
