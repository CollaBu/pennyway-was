package kr.co.pennyway.api.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@ActiveProfiles("test")
public abstract class ExternalApiDBTestConfig {
    private static final String REDIS_CONTAINER_IMAGE = "redis:7.2.4-alpine";
    private static final String MYSQL_CONTAINER_IMAGE = "mysql:8.0.26";

    private static final RedisContainer REDIS_CONTAINER;
    private static final MySQLContainer<?> MYSQL_CONTAINER;

    static {
        REDIS_CONTAINER =
                new RedisContainer(DockerImageName.parse(REDIS_CONTAINER_IMAGE))
                        .withExposedPorts(6379)
                        .withReuse(true);
        MYSQL_CONTAINER =
                new MySQLContainer<>(DockerImageName.parse(MYSQL_CONTAINER_IMAGE))
                        .withDatabaseName("pennyway")
                        .withUsername("root")
                        .withPassword("testpass")
                        .withReuse(true);

        REDIS_CONTAINER.start();
        MYSQL_CONTAINER.start();
    }

    @DynamicPropertySource
    public static void setRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(REDIS_CONTAINER.getMappedPort(6379)));
        registry.add("spring.datasource.url", () -> String.format("jdbc:mysql://%s:%s/pennyway?serverTimezone=UTC&characterEncoding=utf8", MYSQL_CONTAINER.getHost(), MYSQL_CONTAINER.getMappedPort(3306)));
        registry.add("spring.datasource.username", () -> "root");
        registry.add("spring.datasource.password", () -> "testpass");
    }
}
