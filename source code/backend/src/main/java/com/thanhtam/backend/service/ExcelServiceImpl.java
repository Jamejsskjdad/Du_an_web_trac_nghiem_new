package com.thanhtam.backend.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.thanhtam.backend.dto.UserExport;
import com.thanhtam.backend.entity.Intake;
import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.Role;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.UserRepository;
import com.thanhtam.backend.ultilities.ERole;

@Service
public class ExcelServiceImpl implements ExcelService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelServiceImpl.class);
    private final Path root = Paths.get("uploads");
    private FilesStorageService filesStorageService;
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private RoleService roleService;
    private IntakeService intakeService;

    @Autowired
    public ExcelServiceImpl(FilesStorageService filesStorageService, PasswordEncoder passwordEncoder, UserRepository userRepository, RoleService roleService, IntakeService intakeService) {
        this.filesStorageService = filesStorageService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.intakeService = intakeService;
    }

    @Override
    public List<User> readUserFromExcelFile(String excelFilePath) throws IOException {
        List<User> userList = new ArrayList<>();
        FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
    
        Workbook workbook = getWorkbook(inputStream, excelFilePath);
        Sheet firstSheet = workbook.getSheetAt(0);
        Iterator<Row> rows = firstSheet.iterator();
    
        int rowIndex = 0;
        while (rows.hasNext()) {
            Row row = rows.next();
    
            // Bỏ qua dòng tiêu đề
            if (rowIndex++ == 0) continue;
    
            User user = new User();
            Profile profile = new Profile();
    
            String username = getStringCell(row.getCell(0));
            String passwordRaw = getStringCell(row.getCell(1)); // vẫn lấy từ username nếu cần
            String email = getStringCell(row.getCell(2));
            String firstName = getStringCell(row.getCell(3));
            String lastName = getStringCell(row.getCell(4));
            String intakeCode = getStringCell(row.getCell(5));
            String roleStr = getStringCell(row.getCell(6));
    
            if (username == null || email == null || roleStr == null) {
                // Bỏ qua nếu thiếu thông tin bắt buộc
                continue;
            }
    
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(passwordRaw != null ? passwordRaw : username)); // fallback nếu ô password trống
            user.setEmail(email);
    
            profile.setFirstName(firstName);
            profile.setLastName(lastName);
            user.setProfile(profile);
    
            // Xử lý intake nếu có
            // ↓ Trong xử lý intake, dùng lại luôn:
            if (intakeCode != null && !intakeCode.isBlank()) {
                try {
                    Intake intake = intakeService.findByCode(intakeCode);
                    if (intake != null) {
                        user.setIntake(intake);
                    } else {
                        LOGGER.warn("Intake code không tồn tại: " + intakeCode + " → bỏ qua user");
                        continue;
                    }
                } catch (Exception e) {
                    LOGGER.warn("Lỗi khi tìm Intake code " + intakeCode + ": " + e.getMessage());
                    continue;
                }
            }

            // Xử lý role
            Role userRole;
            switch (roleStr.trim().toUpperCase()) {
                case "ADMIN":
                    userRole = roleService.findByName(ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));
                    break;
                case "LECTURER":
                    userRole = roleService.findByName(ERole.ROLE_LECTURER)
                            .orElseThrow(() -> new RuntimeException("Role LECTURER not found"));
                    break;
                default:
                    userRole = roleService.findByName(ERole.ROLE_STUDENT)
                            .orElseThrow(() -> new RuntimeException("Role STUDENT not found"));
                    break;
            }
            user.setRoles(Set.of(userRole));
    
            userList.add(user);
        }
    
        workbook.close();
        inputStream.close();
        LOGGER.info("Imported user list size: " + userList.size());
        return userList;
    }
    
    // Hàm lấy dữ liệu kiểu String từ cell
    private String getStringCell(Cell cell) {
        if (cell == null) return null;
        cell.setCellType(org.apache.poi.ss.usermodel.CellType.STRING);
        return cell.getStringCellValue().trim();
    }
    

    @Override
    public void writeUserToExcelFile(ArrayList<UserExport> userExports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            String[] columns = {"Username", "Họ và tên", "Email"};
            Sheet sheet = workbook.createSheet("List of users");
            //Custom style
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerFont.setColor(IndexedColors.RED.getIndex());

            // Create a CellStyle with the font
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // Create a Row
            Row headerRow = sheet.createRow(0);

            // Create cells
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Create Other rows and cells with employees data
            int rowNum = 1;
            for (UserExport user : userExports) {
                Row row = sheet.createRow(rowNum++);

                // Employee's name (Column A)
                row.createCell(0)
                        .setCellValue(user.getUsername());

                // Employee's email (Column B)
                row.createCell(1)
                        .setCellValue(user.getFirstName());

                row.createCell(2)
                        .setCellValue(user.getLastName());

                row.createCell(3)
                        .setCellValue(user.getEmail());
            }
            // Making size of column auto resize to fit with data
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            sheet.autoSizeColumn(3);
            FileOutputStream fileOut = new FileOutputStream("users.xlsx");

            workbook.write(fileOut);
            fileOut.close();

        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    @Override
    public void InsertUserToDB(List<User> userList) {
        userList.forEach(user -> {
            if (userRepository.existsByEmailOrUsername(user.getEmail(), user.getUsername())) {
                try {
                    throw new Exception("Username or email has already existed");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                userRepository.save(user);
            }
        });

    }

    private Object getCellValue(Cell cell) {
    switch (cell.getCellType()) {
        case STRING:
            return cell.getStringCellValue();
        case BOOLEAN:
            return cell.getBooleanCellValue();
        case NUMERIC:
            return cell.getNumericCellValue();
        default:
            return null;
    }
}


    private Workbook getWorkbook(FileInputStream inputStream, String excelFilePath) throws IOException {
        Workbook workbook = null;

        if (excelFilePath.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        } else if (excelFilePath.endsWith("xls")) {
            workbook = new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("The specified file is not Excel file");
        }

        return workbook;
    }
}
