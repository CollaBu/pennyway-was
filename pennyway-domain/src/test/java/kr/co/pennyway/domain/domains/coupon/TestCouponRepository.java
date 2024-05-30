package kr.co.pennyway.domain.domains.coupon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public interface TestCouponRepository extends JpaRepository<TestCoupon, Long> {
}
