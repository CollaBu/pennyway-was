package kr.co.pennyway.domain.config;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@ActiveProfiles("test")
public class ContainerMySqlTestConfig {
    private static final String MYSQL_CONTAINER_IMAGE = "mysql:8.0.26";

    private static final MySQLContainer<?> MYSQL_CONTAINER;

    static {
        MYSQL_CONTAINER =
                new MySQLContainer<>(DockerImageName.parse(MYSQL_CONTAINER_IMAGE))
                        .withDatabaseName("pennyway")
                        .withUsername("root")
                        .withPassword("testpass")
                        .withReuse(true);

        MYSQL_CONTAINER.start();
    }

    @DynamicPropertySource
    public static void setRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> String.format("jdbc:mysql://%s:%s/pennyway?serverTimezone=UTC&characterEncoding=utf8", MYSQL_CONTAINER.getHost(), MYSQL_CONTAINER.getMappedPort(3306)));
        registry.add("spring.datasource.username", () -> "root");
        registry.add("spring.datasource.password", () -> "testpass");
    }
}
