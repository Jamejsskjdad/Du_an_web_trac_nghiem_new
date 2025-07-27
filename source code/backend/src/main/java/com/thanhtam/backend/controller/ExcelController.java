package com.thanhtam.backend.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.thanhtam.backend.dto.UserExcel;
import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.service.ExcelService;
import com.thanhtam.backend.service.FilesStorageService;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "/api")
public class ExcelController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelController.class);
    private FilesStorageService filesStorageService;
    private ExcelService excelService;

    @Autowired
    public ExcelController(FilesStorageService filesStorageService, ExcelService excelService) {
        this.filesStorageService = filesStorageService;
        this.excelService = excelService;
    }

    @PostMapping("/file/import/users")
    public ResponseEntity<UserExcel> uploadUserToDB(@RequestParam("file") MultipartFile file) throws IOException {
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
        String filePath = uploadDir + "/user-import-" + System.currentTimeMillis() + ".xlsx";
    
        try {
            // ✅ Tạo thư mục nếu chưa tồn tại
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
    
            // ✅ Ghi file
            File savedFile = new File(filePath);
            file.transferTo(savedFile);
    
            // ✅ Đọc & xử lý
            List<User> userList = excelService.readUserFromExcelFile(savedFile.getAbsolutePath());
            excelService.InsertUserToDB(userList);
    
            return ResponseEntity.ok().body(
                new UserExcel(HttpStatus.OK.value(), "Import thành công", userList, userList.size())
            );
        } catch (Exception e) {
            LOGGER.error("Import user thất bại: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                new UserExcel(HttpStatus.EXPECTATION_FAILED.value(), "Import lỗi!", null, 0)
            );
        } finally {
            // ✅ Xóa file tạm nếu tồn tại
            File tmp = new File(filePath);
            if (tmp.exists()) {
                tmp.delete();
            }
        }
    }
    

}
