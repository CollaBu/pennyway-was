package kr.co.infra.config;

import kr.co.infra.common.annotation.InfraRedisConnectionFactory;
import kr.co.infra.common.annotation.OidcCacheManager;
import kr.co.infra.common.annotation.SecurityUserCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {
    private final String host;
    private final int port;
    private final String password;

    private final long defaultCacheTtlSec = 60;
    private final long securityUserCacheTtlSec = 30;
    private final long oidcCacheTtlDay = 7;

    public CacheConfig(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.password}") String password
    ) {
        this.host = host;
        this.port = port;
        this.password = password;
    }

    @Bean
    @InfraRedisConnectionFactory
    public RedisConnectionFactory infraRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        config.setPassword(password);
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder().build();
        return new LettuceConnectionFactory(config, clientConfig);
    }

    /**
     * CacheManager를 명시하지 않을 경우 default로 사용되는 CacheManager
     */
    @Bean
    @Primary
    public CacheManager defaultCacheManager(@InfraRedisConnectionFactory RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .computePrefixWith(CacheKeyPrefix.simple())
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                .entryTtl(Duration.ofSeconds(defaultCacheTtlSec));

        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }

    @Bean
    @SecurityUserCacheManager
    public CacheManager securityUserCacheManager(@InfraRedisConnectionFactory RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .computePrefixWith(CacheKeyPrefix.simple())
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                .entryTtl(Duration.ofSeconds(securityUserCacheTtlSec));
        Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = Map.of("securityConfig", config);

        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(redisCacheConfigurationMap)
                .build();
    }

    @Bean
    @OidcCacheManager
    public CacheManager oidcCacheManger(@InfraRedisConnectionFactory RedisConnectionFactory cf) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        ))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        ))
                .entryTtl(Duration.ofDays(oidcCacheTtlDay));
        Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = Map.of("oidcConfig", config);

        return RedisCacheManager
                .RedisCacheManagerBuilder
                .fromConnectionFactory(cf)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(redisCacheConfigurationMap)
                .build();
    }
}
