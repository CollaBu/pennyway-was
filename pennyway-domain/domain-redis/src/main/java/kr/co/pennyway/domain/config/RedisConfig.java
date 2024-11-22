package kr.co.pennyway.domain.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.co.pennyway.domain.common.importer.PennywayRedisDomainConfig;
import org.springframework.context.annotation.Bean;

public class RedisConfig implements PennywayRedisDomainConfig {
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }
}
