package kr.co.pennyway.domain.domains.coupon;

import kr.co.pennyway.domain.common.aop.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Component
@ActiveProfiles("test")
@RequiredArgsConstructor
public class TestCouponDecreaseService {
    private final TestCouponRepository couponRepository;

    @Transactional
    public void decreaseStock(Long couponId) {
        TestCoupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        coupon.decreaseStock();
    }

    @DistributedLock(key = "#lockName")
    public void decreaseStockWithLock(Long couponId, String lockName) {
        TestCoupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        coupon.decreaseStock();
    }
}
