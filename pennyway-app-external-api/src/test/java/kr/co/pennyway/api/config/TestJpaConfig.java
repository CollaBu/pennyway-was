package kr.co.pennyway.api.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

@TestConfiguration
public class TestJpaConfig {
    @PersistenceContext
    private EntityManager em;

    @Bean
    @ConditionalOnMissingBean
    public JPAQueryFactory testJpaQueryFactory() {
        return new JPAQueryFactory(em);
    }

    @Bean
    @ConditionalOnMissingBean
    public SQLTemplates testSqlTemplates() {
        return new MySQLTemplates();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<String, ?> testRedisTemplate() {
        return null;
    }
}