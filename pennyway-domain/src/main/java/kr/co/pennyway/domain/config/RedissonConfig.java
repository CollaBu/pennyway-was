package kr.co.pennyway.domain.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    private static final String REDISSON_HOST_PREFIX = "redis://";
    private final String host;
    private final int port;
    private final String password;

    public RedissonConfig(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.password}") String password
    ) {
        this.host = host;
        this.port = port;
        this.password = password;
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(REDISSON_HOST_PREFIX + host + ":" + port)
                .setPassword(password);
        return Redisson.create(config);
    }
}