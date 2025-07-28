package com.thanhtam.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.thanhtam.backend.dto.UserExport;
import com.thanhtam.backend.dto.UserPasswordExportDTO;
import com.thanhtam.backend.entity.User;

public interface UserService {

    Optional<User> getUserByUsername(String username);

    String getUserName();

    Boolean existsByUsername(String username);

    Page<User> findUsersByPage(Pageable pageable);

    Page<User> findUsersDeletedByPage(Pageable pageable, boolean deleted);

    Page<User> findAllByDeletedAndUsernameContains(boolean deleted, String username, Pageable pageable);

    User createUser(User user);

    Optional<User> findUserById(Long id);

    List<UserExport> findAllByDeletedToExport(boolean deleted);

    void updateUser(User user);

    List<User> findAllByIntakeId(Long id);

    List<UserPasswordExportDTO> generatePasswordsForStudents(Long intakeId);

    void deleteUserById(Long userId, Long currentAdminId);

    void deleteManyUsersByIds(List<Long> userIds, Long currentAdminId);

    void updateUserIcon(Long id, String icon);
}
