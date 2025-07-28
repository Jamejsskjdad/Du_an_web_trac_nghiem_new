package com.thanhtam.backend.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
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

            if (rowIndex++ == 0) continue;

            User user = new User();
            Profile profile = new Profile();

            String username = getStringCell(row.getCell(0));
            String passwordRaw = getStringCell(row.getCell(1));
            String firstName = getStringCell(row.getCell(2));
            String lastName = getStringCell(row.getCell(3));
            String birthDateStr = getStringCell(row.getCell(4));
            String intakeCode = getStringCell(row.getCell(5));
            String roleStr = getStringCell(row.getCell(6));

            if (username == null || roleStr == null) {
                continue;
            }

            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(passwordRaw != null ? passwordRaw : username));

            profile.setFirstName(firstName);
            profile.setLastName(lastName);
            user.setProfile(profile);

            if (birthDateStr != null && !birthDateStr.isEmpty()) {
                try {
                    Date birthDate = getDateCell(row.getCell(4));
                    user.setBirthDate(birthDate);                    
                } catch (Exception e) {
                    LOGGER.warn("Lỗi định dạng ngày sinh: " + birthDateStr);
                }
            }

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

            Role userRole;
            switch (roleStr.trim().toUpperCase()) {
                case "ADMIN":
                    userRole = roleService.findByName(ERole.ROLE_ADMIN).orElseThrow();
                    break;
                case "LECTURER":
                    userRole = roleService.findByName(ERole.ROLE_LECTURER).orElseThrow();
                    break;
                default:
                    userRole = roleService.findByName(ERole.ROLE_STUDENT).orElseThrow();
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

    private String getStringCell(Cell cell) {
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }
    private Date getDateCell(Cell cell) {
        if (cell == null) return null;
    
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getDateCellValue(); // Trường hợp ngày chuẩn Excel
            } else if (cell.getCellType() == CellType.STRING) {
                String str = cell.getStringCellValue().trim();
                if (!str.isEmpty()) {
                    // Trường hợp chuỗi là số serial như "38698"
                    if (str.matches("\\d{4,5}")) {
                        double serial = Double.parseDouble(str);
                        return org.apache.poi.ss.usermodel.DateUtil.getJavaDate(serial);
                    }
    
                    // Trường hợp chuỗi theo định dạng dd-MM-yyyy hoặc MM/dd/yyyy
                    String[] patterns = {"dd-MM-yyyy", "MM/dd/yyyy", "yyyy-MM-dd"};
                    for (String pattern : patterns) {
                        try {
                            return new SimpleDateFormat(pattern).parse(str);
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Không thể parse ngày sinh từ ô (loại={}): {}", cell.getCellType(), cell.toString());
        }
    
        return null;
    }
    
    
    
    
    @Override
    public void writeUserToExcelFile(ArrayList<UserExport> userExports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            String[] columns = {"Username", "Họ", "Tên", "Ngày sinh"};
            Sheet sheet = workbook.createSheet("List of users");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerFont.setColor(IndexedColors.RED.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowNum = 1;
            for (UserExport user : userExports) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getUsername());
                row.createCell(1).setCellValue(user.getFirstName());
                row.createCell(2).setCellValue(user.getLastName());
                row.createCell(3).setCellValue(user.getBirthDate() != null ? user.getBirthDate().toString() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            FileOutputStream fileOut = new FileOutputStream("users.xlsx");
            workbook.write(fileOut);
            fileOut.close();
        }
    }

    @Override
    public void InsertUserToDB(List<User> userList) {
        userList.forEach(user -> {
            if (userRepository.existsByUsername(user.getUsername())) {
                LOGGER.warn("Username đã tồn tại: " + user.getUsername());
            } else {
                userRepository.save(user);
            }
        });
    }

    private Workbook getWorkbook(FileInputStream inputStream, String excelFilePath) throws IOException {
        if (excelFilePath.endsWith("xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else if (excelFilePath.endsWith("xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("The specified file is not Excel file");
        }
    }
}
