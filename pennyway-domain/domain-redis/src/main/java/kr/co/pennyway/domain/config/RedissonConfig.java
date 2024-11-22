package kr.co.pennyway.domain.config;

import kr.co.pennyway.domain.common.aop.CallTransactionFactory;
import kr.co.pennyway.domain.common.aop.DistributedLockAspect;
import kr.co.pennyway.domain.common.aop.RedissonCallNewTransaction;
import kr.co.pennyway.domain.common.aop.RedissonCallSameTransaction;
import kr.co.pennyway.domain.common.importer.PennywayRdsDomainConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class RedissonConfig implements PennywayRdsDomainConfig {
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

    @Bean
    public RedissonCallNewTransaction redissonCallNewTransaction() {
        return new RedissonCallNewTransaction();
    }

    @Bean
    public RedissonCallSameTransaction redissonCallSameTransaction() {
        return new RedissonCallSameTransaction();
    }

    @Bean
    public CallTransactionFactory callTransactionFactory(RedissonCallNewTransaction redissonCallNewTransaction, RedissonCallSameTransaction redissonCallSameTransaction) {
        return new CallTransactionFactory(redissonCallNewTransaction, redissonCallSameTransaction);
    }

    @Bean
    public DistributedLockAspect distributedLockAspect(RedissonClient redissonClient, CallTransactionFactory callTransactionFactory) {
        return new DistributedLockAspect(redissonClient, callTransactionFactory);
    }
}