package com.thanhtam.backend.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.repository.RoleRepository;
import com.thanhtam.backend.ultilities.ERole;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<Role> findByName(ERole name) {
        return roleRepository.findByName(name.name()); // ép kiểu enum → String
    }

    @Override
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }
}
