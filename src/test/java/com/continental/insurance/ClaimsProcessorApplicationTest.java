package com.continental.insurance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring Boot context load test — verifies the application starts successfully.
 */
@SpringBootTest
class ClaimsProcessorApplicationTest {

    @Test
    @DisplayName("Spring Boot application context loads successfully")
    void contextLoads() {
        // Verifies all beans are wired correctly and the application context starts
    }
}
