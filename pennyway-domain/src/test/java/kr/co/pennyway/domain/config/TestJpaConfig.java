package kr.co.pennyway.domain.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.co.pennyway.domain.domains.coupon.TestCoupon;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@EntityScan(basePackageClasses = TestCoupon.class)
public class TestJpaConfig {
    @PersistenceContext
    private EntityManager em;

    @ConditionalOnMissingBean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }
}
