package kr.co.pennyway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

@TestConfiguration
public class RedisDataTestConfig {
    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<String, ?> testRedisTemplate() {
        return null;
    }
}
