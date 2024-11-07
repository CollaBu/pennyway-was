package kr.co.pennyway.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

@Configuration
@RequiredArgsConstructor
public class BatchRedisConfig {
    private static final String PREFIX_PATTERN = "chat:last_read:*";
    private final RedisTemplate<String, String> redisTemplate;

    @Bean
    public Cursor<String> lastMessageIdCursor() {
        ScanOptions options = ScanOptions.scanOptions().match(PREFIX_PATTERN).count(1000).build();
        return redisTemplate.scan(options);
    }
}
