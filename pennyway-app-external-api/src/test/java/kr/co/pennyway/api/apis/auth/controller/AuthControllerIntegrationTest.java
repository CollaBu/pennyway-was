package kr.co.pennyway.api.apis.auth.controller;

import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.ExternalApiIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@ExternalApiIntegrationTest
public class AuthControllerIntegrationTest extends ExternalApiDBTestConfig {
    @Test
    @DisplayName("컨테이너 실행 테스트")
    void containerTest() {
        System.out.println("컨테이너 실행 테스트");
    }
}