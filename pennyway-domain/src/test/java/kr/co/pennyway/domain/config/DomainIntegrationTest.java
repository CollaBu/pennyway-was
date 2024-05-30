package kr.co.pennyway.domain.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = DomainIntegrationTestConfig.class)
@ActiveProfiles(profiles = {"test"}, resolver = DomainIntegrationProfileResolver.class)
@Documented
public @interface DomainIntegrationTest {
}
