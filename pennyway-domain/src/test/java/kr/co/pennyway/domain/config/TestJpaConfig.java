package kr.co.pennyway.domain.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestJpaConfig {
    @PersistenceContext
    private EntityManager em;

    @Bean
    @ConditionalOnMissingBean
    public JPAQueryFactory testJpaQueryFactory() {
        return new JPAQueryFactory(em);
    }
}
