package com.thanhtam.backend.controller;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.thanhtam.backend.service.S3Services;
import com.thanhtam.backend.service.UserService;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(value = "/api/aws")
public class FileAWSController {
    S3Services s3Services;
    UserService userService;

    Logger logger = LoggerFactory.getLogger(FileAWSController.class);

    @Autowired
    public FileAWSController(S3Services s3Services, UserService userService) {
        this.s3Services = s3Services;
        this.userService = userService;
    }

    @Value("${isc.s3.bucket}")
    private String bucketName;

    @Value("${isc.aws.endpointUrl}")
    private String endpointUrl;

    // ❌ Đã bỏ: upload avatar ảnh đại diện (không cần nữa)

    @PostMapping("/file/upload/course")
    public String uploadCourseImg(@RequestParam("file") MultipartFile file) {
        String keyName = new Date().toString().concat(file.getOriginalFilename());
        s3Services.uploadS3File(keyName, file);
        return endpointUrl + "/" + bucketName + "/" + keyName;
    }

    @PostMapping("/file/upload")
    public String uploadGenericFile(@RequestParam("file") MultipartFile file,
                                    @RequestParam String fileAs) {
        String keyName;
        switch (fileAs) {
            case "course": {
                keyName = new Date().toString().concat(file.getOriginalFilename());
                break;
            }
            default: {
                keyName = file.getOriginalFilename();
                break;
            }
        }

        s3Services.uploadS3File(keyName, file);
        return endpointUrl + "/" + bucketName + "/" + keyName;
    }

    @GetMapping("/file/{keyName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String keyName) {
        ByteArrayOutputStream downloadInputStream = s3Services.downloadS3File(keyName);

        return ResponseEntity.ok()
                .contentType(contentType(keyName))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + keyName + "\"")
                .body(downloadInputStream.toByteArray());
    }

    @GetMapping("/file/all")
    public List<String> listAllFiles() {
        return s3Services.listS3Files();
    }

    private MediaType contentType(String keyName) {
        String[] arr = keyName.split("\\.");
        String type = arr[arr.length - 1];
        switch (type) {
            case "txt":
                return MediaType.TEXT_PLAIN;
            case "png":
                return MediaType.IMAGE_PNG;
            case "jpg":
                return MediaType.IMAGE_JPEG;
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    @DeleteMapping(value = "/delete")
    public ResponseEntity<String> deleteFile(@RequestParam(value = "fileName") final String keyName) {
        s3Services.deleteFile(keyName);
        final String response = "[" + keyName + "] deleted successfully.";
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
