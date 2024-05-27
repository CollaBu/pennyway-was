package kr.co.pennyway.domain.domains.coupon;

import jakarta.persistence.EntityManager;
import kr.co.pennyway.domain.common.aop.DistributedLock;
import org.springframework.transaction.annotation.Transactional;

public class TestCouponDecreaseService {
    private final EntityManager em;

    public TestCouponDecreaseService(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public void decreaseStock(Long couponId) {
        TestCoupon coupon = em.find(TestCoupon.class, couponId);

        if (coupon == null) {
            throw new IllegalArgumentException("존재하지 않는 쿠폰입니다.");
        }

        coupon.decreaseStock();
    }

    @DistributedLock(key = "#lockName")
    public void decreaseStockWithLock(Long couponId, String lockName) {
        TestCoupon coupon = em.find(TestCoupon.class, couponId);

        if (coupon == null) {
            throw new IllegalArgumentException("존재하지 않는 쿠폰입니다.");
        }

        coupon.decreaseStock();
    }
}
