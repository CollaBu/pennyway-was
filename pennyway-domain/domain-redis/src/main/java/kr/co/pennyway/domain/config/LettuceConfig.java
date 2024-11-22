package kr.co.pennyway.domain.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.pennyway.domain.RedisPackageLocation;
import kr.co.pennyway.domain.common.annotation.DomainRedisCacheManager;
import kr.co.pennyway.domain.common.annotation.DomainRedisConnectionFactory;
import kr.co.pennyway.domain.common.annotation.DomainRedisTemplate;
import kr.co.pennyway.domain.common.importer.PennywayRdsDomainConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Duration;

@EnableRedisRepositories(basePackageClasses = RedisPackageLocation.class)
@EnableTransactionManagement
public class LettuceConfig implements PennywayRdsDomainConfig {
    private final String host;
    private final int port;
    private final String password;

    public LettuceConfig(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.password}") String password
    ) {
        this.host = host;
        this.port = port;
        this.password = password;
    }

    @Bean
    @DomainRedisConnectionFactory
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        config.setPassword(password);
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder().build();
        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean
    @Primary
    @DomainRedisTemplate
    public RedisTemplate<String, ?> redisTemplate(ObjectMapper redisObjectMapper) {
        RedisTemplate<String, ?> template = new RedisTemplate<>();

        template.setConnectionFactory(redisConnectionFactory());

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper));
        return template;
    }

    @Bean
    @DomainRedisCacheManager
    public RedisCacheManager redisCacheManager(@DomainRedisConnectionFactory RedisConnectionFactory cf) {
        RedisCacheConfiguration redisCacheConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new StringRedisSerializer()))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new GenericJackson2JsonRedisSerializer()))
                        .entryTtl(Duration.ofHours(1L));

        return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(cf)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }
}
