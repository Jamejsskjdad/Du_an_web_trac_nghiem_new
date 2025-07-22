package com.thanhtam.backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.thanhtam.backend.config.JwtUtils;
import com.thanhtam.backend.dto.UserExport;
import com.thanhtam.backend.dto.UserPasswordExportDTO;
import com.thanhtam.backend.entity.PasswordResetToken;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.ExamRepository;
import com.thanhtam.backend.repository.ExamUserRepository;
import com.thanhtam.backend.repository.PasswordResetTokenRepository;
import com.thanhtam.backend.repository.QuestionRepository;
import com.thanhtam.backend.repository.RoleUserRepository;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.ultilities.ERole;


@Service(value = "userService")
public class UserServiceImpl implements UserService {
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_LECTURER = "ROLE_LECTURER";
    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private UserRepository userRepository;
    private RoleService roleService;
    private PasswordEncoder passwordEncoder;
    private PasswordResetTokenRepository passwordResetTokenRepository;
    private EmailService emailService;
    @Autowired
    private ExamUserRepository examUserRepository;
    
    @Autowired
    private RoleUserRepository roleUserRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private ExamRepository examRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder, PasswordResetTokenRepository passwordResetTokenRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }


    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public String getUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return username;
    }

    @Override
    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
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
        // Mã hóa password và lưu lại cho mục đích debug
        String rawPassword = user.getPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password không được để trống!");
        }
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        // Xử lý roles
        Set<Role> reqRoles = user.getRoles();
        Set<Role> roles = new HashSet<>();
        if (reqRoles == null) {
            Role userRole = roleService.findByName(ERole.ROLE_STUDENT).orElseThrow(() -> new RuntimeException("Error: Role is not found"));
            roles.add(userRole);
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

        // Lưu user vào DB
        User savedUser = userRepository.save(user);

        // Tạo Map trả về cho mục đích debug
        Map<String, String> debugMap = new HashMap<>();
        debugMap.put("rawPassword", rawPassword);
        debugMap.put("passwordHash", encodedPassword);

        // Log thông tin debug
        logger.info("Debug Info: " + debugMap);

        // Trả về user đã lưu (hoặc có thể trả về cả debugMap nếu cần)
        return savedUser;
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<UserExport> findAllByDeletedToExport(boolean statusDelete) {
        List<User> userList = userRepository.findAllByDeleted(statusDelete);
        List<UserExport> userExportList = new ArrayList<>();
        userList.forEach(user -> {
            // Kiểm tra user có profile không
            if (user.getProfile() != null) {
                userExportList.add(
                    new UserExport(
                        user.getUsername(),
                        user.getEmail(),
                        user.getProfile().getFirstName(),
                        user.getProfile().getLastName()
                    )
                );
            } else {
                // Có thể log ra nếu muốn
                logger.warn("User '{}' không có profile, bỏ qua khi export!", user.getUsername());
            }
        });
        return userExportList;
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
    public boolean requestPasswordReset(String email) throws MessagingException {
        boolean returnValue = false;
        Optional<User> user = userRepository.findByEmail(email);
        if (!user.isPresent()) {
            return false;
        }
        String token = new JwtUtils().generatePasswordResetToken(user.get().getId());
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setUser(user.get());

        passwordResetTokenRepository.save(passwordResetToken);
        emailService.resetPassword(email, token);
        return true;
    }

    @Override
    public boolean resetPassword(String token, String password) {
        boolean returnValue = false;
        logger.error(token);
        if (new JwtUtils().hasTokenExpired(token)) {
            return false;
        }
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if (passwordResetToken == null) {
            return false;
        }

//        Prepare a new password
        String encodedPassword = passwordEncoder.encode(password);

//        Update user password into database
        User user = passwordResetToken.getUser();
        user.setPassword(encodedPassword);
        User userSave = userRepository.save(user);

//        verify if password was saved
        if(userSave !=null && userSave.getPassword().equalsIgnoreCase(encodedPassword)){
            returnValue = true;
        }
        passwordResetTokenRepository.delete(passwordResetToken);
        return returnValue;
    }

    @Override
    public Page<User> findAllByUsernameContainsOrEmailContains(String username, String email, Pageable pageable) {
        return userRepository.findAllByUsernameContainsOrEmailContains(username, email, pageable);
    }

    public void addRoles(ERole roleName, Set<Role> roles) {
        Role userRole = roleService.findByName(roleName).orElseThrow(() -> new RuntimeException("Error: Role is not found"));
        roles.add(userRole);
    }
    private String generateRandomPassword(int length) {
        // Loại bỏ ký tự dễ nhầm lẫn (I, l, 1, O, 0)
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
        StringBuilder password = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return password.toString();
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
    public void deleteManyUsersByIds(List<Long> userIds, Long currentAdminId) {
        List<User> users = userRepository.findAllById(userIds);
        for (User user : users) {
            // Không cho phép tự xóa mình!
            if (user.getId().equals(currentAdminId)) {
                throw new IllegalArgumentException("Bạn không thể tự xóa chính mình!");
            }

            boolean isAdminOrLecturer = user.getRoles().stream()
                    .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()) || "ROLE_LECTURER".equals(role.getName()));
            if (isAdminOrLecturer) {
                // Cập nhật liên kết created_by_id của question, exam
                questionRepository.updateCreatedByForQuestions(user.getId(), currentAdminId);
                examRepository.updateCreatedByForExams(user.getId(), currentAdminId);
            }
        }
        examUserRepository.deleteAllByUserIds(userIds);
        roleUserRepository.deleteAllByUserIds(userIds);
        userRepository.deleteAllById(userIds);
    }
    @Override
    public void deleteUserById(Long userId, Long currentAdminId) {
        // Có thể tái sử dụng logic của deleteManyUsersByIds cho 1 user
        List<Long> ids = new ArrayList<>();
        ids.add(userId);
        deleteManyUsersByIds(ids, currentAdminId);
    }
    

}
