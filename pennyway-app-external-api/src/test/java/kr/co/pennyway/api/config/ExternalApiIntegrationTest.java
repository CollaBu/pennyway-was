package kr.co.pennyway.api.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = ExternalApiIntegrationTestConfig.class)
@ActiveProfiles(resolver = ExternalApiIntegrationProfileResolver.class)
@Documented
public @interface ExternalApiIntegrationTest {
}
