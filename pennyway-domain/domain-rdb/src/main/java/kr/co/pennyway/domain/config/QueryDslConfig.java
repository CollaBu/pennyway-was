package kr.co.pennyway.domain.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.co.pennyway.domain.common.importer.PennywayRdbDomainConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

public class QueryDslConfig implements PennywayRdbDomainConfig {
    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    @Primary
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    public SQLTemplates sqlTemplates() {
        return new MySQLTemplates();
    }
}
