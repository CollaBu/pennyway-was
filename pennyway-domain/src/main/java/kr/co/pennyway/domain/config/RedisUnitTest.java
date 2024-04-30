package kr.co.pennyway.domain.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ComponentScan(basePackages = "kr.co.pennyway.domain.common.redis")
@EnableAutoConfiguration
@EnableRedisRepositories(basePackages = "kr.co.pennyway.domain.common.redis")
@Documented
public @interface RedisUnitTest {
}
