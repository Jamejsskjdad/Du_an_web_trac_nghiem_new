package com.thanhtam.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = BackendApplication.class)
public class BackendApplicationTests {

    public BackendApplicationTests() {
        // Constructor phải public
    }

    @Test
    public void contextLoads() {
        // Đây là test để kiểm tra Spring Boot context khởi động
    }
}
