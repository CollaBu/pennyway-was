package kr.co.pennyway.batch.config;

import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SpringBatchTest
@SpringBootTest(classes = BatchIntegrationTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"}, resolver = BatchIntegrationProfileResolver.class)
@Documented
public @interface BatchIntegrationTest {
}
