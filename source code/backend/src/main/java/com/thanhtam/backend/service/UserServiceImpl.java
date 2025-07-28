package com.thanhtam.backend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.thanhtam.backend.dto.UserExport;
import com.thanhtam.backend.dto.UserPasswordExportDTO;
import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.ExamRepository;
import com.thanhtam.backend.repository.ExamUserRepository;
import com.thanhtam.backend.repository.ProfileRepository;
import com.thanhtam.backend.repository.QuestionRepository;
import com.thanhtam.backend.repository.RoleUserRepository;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.ultilities.ERole;

@Service(value = "userService")
public class UserServiceImpl implements UserService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_LECTURER = "ROLE_LECTURER";

    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Autowired private ExamUserRepository examUserRepository;
    @Autowired private RoleUserRepository roleUserRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private ExamRepository examRepository;
    @Autowired private ProfileRepository profileRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleService roleService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public String getUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    @Override
    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Page<User> findUsersByPage(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> findUsersDeletedByPage(Pageable pageable, boolean deleted) {
        return userRepository.findAllByDeleted(deleted, pageable);
    }

    @Override
    public Page<User> findAllByDeletedAndUsernameContains(boolean deleted, String username, Pageable pageable) {
        return userRepository.findAllByDeletedAndUsernameContains(deleted, username, pageable);
    }

    @Override
    public User createUser(User user) {
        String rawPassword = user.getPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password không được để trống!");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        // Gán role
        Set<Role> roles = new HashSet<>();
        Set<Role> reqRoles = user.getRoles();

        if (reqRoles == null) {
            addRoles(ERole.ROLE_STUDENT, roles);
        } else {
            reqRoles.forEach(role -> {
                switch (role.getName()) {
                    case ROLE_ADMIN:
                        addRoles(ERole.ROLE_ADMIN, roles);
                        break;
                    case ROLE_LECTURER:
                        addRoles(ERole.ROLE_LECTURER, roles);
                        break;
                    default:
                        addRoles(ERole.ROLE_STUDENT, roles);
                }
            });
        }
        user.setRoles(roles);

        // Lưu user
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<UserExport> findAllByDeletedToExport(boolean deleted) {
        List<User> userList = userRepository.findAllByDeleted(deleted);
        List<UserExport> exportList = new ArrayList<>();

        for (User user : userList) {
            if (user.getProfile() != null) {
                exportList.add(new UserExport(
                    user.getUsername(),
                    user.getProfile().getFirstName(),
                    user.getProfile().getLastName(),
                    user.getBirthDate() // Không cần toString()
                ));

            }
        }
        return exportList;
    }

    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Override
    public List<User> findAllByIntakeId(Long id) {
        return userRepository.findAllByIntakeId(id);
    }

    @Override
    public List<UserPasswordExportDTO> generatePasswordsForStudents(Long intakeId) {
        List<User> users = userRepository.findAllByIntakeId(intakeId);
        List<UserPasswordExportDTO> exportList = new ArrayList<>();

        for (User user : users) {
            boolean isStudent = user.getRoles().stream()
                .anyMatch(role -> "ROLE_STUDENT".equals(role.getName()));
            if (!isStudent) continue;

            String rawPass = generateRandomPassword(6);
            String intakeName = user.getIntake() != null ? user.getIntake().getName() : "";

            exportList.add(new UserPasswordExportDTO(user.getUsername(), rawPass, intakeName));
        }
        return exportList;
    }

    @Override
    public void deleteUserById(Long userId, Long currentAdminId) {
        deleteManyUsersByIds(Collections.singletonList(userId), currentAdminId);
    }

    @Override
    public void deleteManyUsersByIds(List<Long> userIds, Long currentAdminId) {
        List<User> users = userRepository.findAllById(userIds);
        for (User user : users) {
            if (user.getId().equals(currentAdminId)) {
                throw new IllegalArgumentException("Bạn không thể tự xóa chính mình!");
            }

            boolean isAdminOrLecturer = user.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()) || "ROLE_LECTURER".equals(role.getName()));

            if (isAdminOrLecturer) {
                questionRepository.updateCreatedByForQuestions(user.getId(), currentAdminId);
                examRepository.updateCreatedByForExams(user.getId(), currentAdminId);
            }
        }

        examUserRepository.deleteAllByUserIds(userIds);
        roleUserRepository.deleteAllByUserIds(userIds);
        userRepository.deleteAllById(userIds);
    }

    @Override
    public void updateUserIcon(Long profileId, String icon) {
        Profile profile = profileRepository.findById(profileId).orElseThrow();
        profile.setIcon(icon);
        profileRepository.save(profile);
    }

    private void addRoles(ERole roleName, Set<Role> roles) {
        Role userRole = roleService.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
        roles.add(userRole);
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
        StringBuilder password = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return password.toString();
    }
}
