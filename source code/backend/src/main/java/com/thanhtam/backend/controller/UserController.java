package com.thanhtam.backend.controller;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.thanhtam.backend.dto.PageResult;
import com.thanhtam.backend.dto.PasswordUpdate;
import com.thanhtam.backend.dto.ServiceResult;
import com.thanhtam.backend.dto.UserCreateRequest;
import com.thanhtam.backend.dto.UserExport;
import com.thanhtam.backend.dto.UserPasswordExportDTO;
import com.thanhtam.backend.dto.UserUpdate;
import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.service.ExcelService;
import com.thanhtam.backend.service.FilesStorageService;
import com.thanhtam.backend.service.IntakeService;
import com.thanhtam.backend.service.RoleService;
import com.thanhtam.backend.service.UserService;

import lombok.extern.slf4j.Slf4j;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "/api/users")
@Slf4j
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private UserService userService;
    private PasswordEncoder passwordEncoder;
    private RoleService roleService;
    private ExcelService excelService;
    FilesStorageService filesStorageService;
    private IntakeService intakeService;

    @Autowired
    public UserController(UserService userService, 
                          RoleService roleService, 
                          PasswordEncoder passwordEncoder, 
                          ExcelService excelService, 
                          FilesStorageService filesStorageService,
                          IntakeService intakeService) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.excelService = excelService;
        this.filesStorageService = filesStorageService;
        this.intakeService = intakeService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUser(@RequestParam String username) {
        Optional<User> user = username.isEmpty() ?
            userService.getUserByUsername(userService.getUserName()) :
            userService.getUserByUsername(username);
        if (!user.isPresent()) {
            return ResponseEntity.ok(new ServiceResult(HttpStatus.NOT_FOUND.value(), "Tên đăng nhâp " + username + " không tìm thấy!", null));
        }
        return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), "Lấy thông tin user " + username + " thành công!", user));
    }

    @GetMapping("/check-username")
    public boolean checkUsername(@RequestParam("value") String value) {
        return userService.existsByUsername(value);
    }

    @PatchMapping("/{id}/password/updating")
    public ResponseEntity updatePass(@Valid @RequestBody PasswordUpdate passwordUpdate, @PathVariable Long id) {
        try {
            log.error(passwordUpdate.toString());
            User user = userService.findUserById(id).get();
            if (passwordEncoder.matches(passwordUpdate.getCurrentPassword(), user.getPassword())) {
                if (!passwordUpdate.getCurrentPassword().equals(passwordUpdate.getNewPassword())) {
                    user.setPassword(passwordEncoder.encode(passwordUpdate.getNewPassword()));
                    userService.updateUser(user);
                    return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), "Update password successfully", null));
                } else {
                    return ResponseEntity.ok(new ServiceResult(HttpStatus.CONFLICT.value(), "This is old password", null));
                }
            } else {
                return ResponseEntity.ok(new ServiceResult(HttpStatus.BAD_REQUEST.value(), "Wrong password, please check again!", null));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/deleted/{deleted}")
    public ResponseEntity<?> deleteTempUser(@PathVariable Long id, @PathVariable boolean deleted) {
        User user = userService.findUserById(id).get();
        user.setDeleted(deleted);
        userService.updateUser(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public PageResult getUsersByPage(@PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
        Page<User> userPage = userService.findUsersByPage(pageable);
        return new PageResult(userPage);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdate userReq, @PathVariable Long id) {
        User userUpdate = userService.findUserById(id).get();
        if (userReq.getPassword() != null) {
            userUpdate.setPassword(passwordEncoder.encode(userReq.getPassword()));
        }
        userUpdate.setBirthDate(userReq.getBirthDate());
        Profile profile = userReq.getProfile();
        profile.setId(userUpdate.getProfile().getId());
        profile.setFirstName(userReq.getProfile().getFirstName());
        profile.setLastName(userReq.getProfile().getLastName());
        userUpdate.setProfile(profile);

        userService.updateUser(userUpdate);
        return ResponseEntity.ok().body(new ServiceResult(HttpStatus.OK.value(), "Cập nhật thành công!", userUpdate));
    }

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateRequest req) {
        if (userService.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body(new ServiceResult(HttpStatus.CONFLICT.value(), "Tên đăng nhập đã có người sử dụng!", ""));
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());
        user.setProfile(req.getProfile());
        user.setBirthDate(req.getBirthDate());

        Set<Role> roles = new HashSet<>();
        Role role = roleService.findById(req.getRoleId())
            .orElseThrow(() -> new RuntimeException("Role not found"));
        roles.add(role);
        user.setRoles(roles);

        if ("ROLE_STUDENT".equals(role.getName()) && req.getIntakeId() != null) {
            Intake intake = intakeService.findById(req.getIntakeId())
                .orElseThrow(() -> new RuntimeException("Intake not found"));
            user.setIntake(intake);
        } else {
            user.setIntake(null);
        }

        userService.createUser(user);
        return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), "User created successfully!", user));
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("deleted/{status}/export/users.xlsx")
    public void exportUsersToExcel(HttpServletResponse response) throws Exception {
        String fileName = "users.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Tên đăng nhập");
        header.createCell(1).setCellValue("Họ");
        header.createCell(2).setCellValue("Tên");
        header.createCell(3).setCellValue("Ngày sinh");

        List<UserExport> userList = userService.findAllByDeletedToExport(false);

        int rowNum = 1;
        for (UserExport user : userList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user.getUsername());
            row.createCell(1).setCellValue(user.getFirstName());
            row.createCell(2).setCellValue(user.getLastName());
            row.createCell(3).setCellValue(user.getBirthDate() != null ? user.getBirthDate().toString() : "");
        }

        for (int i = 0; i < 4; i++) sheet.autoSizeColumn(i);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @PostMapping("/generate-passwords-excel")
    public void generatePasswordsExcel(@RequestParam("intakeId") Long intakeId, HttpServletResponse response) throws Exception {
        List<UserPasswordExportDTO> exportList = userService.generatePasswordsForStudents(intakeId);

        String fileName = "student_passwords.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("StudentPasswords");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Tên đăng nhập");
        header.createCell(1).setCellValue("Mật khẩu");
        header.createCell(2).setCellValue("Lớp (Intake)");

        int rowNum = 1;
        for (UserPasswordExportDTO dto : exportList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getUsername());
            row.createCell(1).setCellValue(dto.getPassword());
            row.createCell(2).setCellValue(dto.getIntakeName());
        }

        for (int i = 0; i < 3; i++) sheet.autoSizeColumn(i);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @PostMapping("/update-passwords-excel")
    public ResponseEntity<?> updatePasswordsFromExcel(@RequestParam("file") MultipartFile file) {
        int updateCount = 0, skipCount = 0;
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null || row.getCell(1) == null) {
                    skipCount++;
                    continue;
                }
                String username = row.getCell(0).getStringCellValue();
                String rawPassword = row.getCell(1).getStringCellValue();

                Optional<User> optUser = userService.getUserByUsername(username);
                if (optUser.isPresent()) {
                    User user = optUser.get();
                    boolean isStudent = user.getRoles().stream()
                        .anyMatch(role -> "ROLE_STUDENT".equals(role.getName()));
                    if (isStudent) {
                        user.setPassword(passwordEncoder.encode(rawPassword));
                        userService.updateUser(user);
                        updateCount++;
                    }
                }
            }
            return ResponseEntity.ok("Cập nhật mật khẩu thành công! " + updateCount + " user được đổi.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi cập nhật mật khẩu: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Principal principal) {
        User currentAdmin = userService.getUserByUsername(principal.getName())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy admin hiện tại"));

        userService.deleteUserById(id, currentAdmin.getId());
        return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), "Xóa user thành công", id));
    }

    @PostMapping("/delete-many")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteManyUsers(@RequestBody List<Long> userIds, Principal principal) {
        try {
            User currentAdmin = userService.getUserByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin hiện tại"));
            userService.deleteManyUsersByIds(userIds, currentAdmin.getId());
            return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), "Đã xóa thành công!", userIds));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ServiceResult(HttpStatus.FORBIDDEN.value(), e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ServiceResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Xóa thất bại: " + e.getMessage(), null));
        }
    }
}
