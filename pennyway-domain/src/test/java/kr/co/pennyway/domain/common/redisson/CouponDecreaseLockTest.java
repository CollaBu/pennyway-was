package kr.co.pennyway.domain.common.redisson;

import kr.co.pennyway.domain.config.ContainerDBTestConfig;
import kr.co.pennyway.domain.config.DomainIntegrationTest;
import kr.co.pennyway.domain.config.TestJpaConfig;
import kr.co.pennyway.domain.domains.coupon.TestCoupon;
import kr.co.pennyway.domain.domains.coupon.TestCouponDecreaseService;
import kr.co.pennyway.domain.domains.coupon.TestCouponRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
@Slf4j
@DomainIntegrationTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EntityScan(basePackageClasses = {TestCoupon.class})
@Import(TestJpaConfig.class)
public class CouponDecreaseLockTest extends ContainerDBTestConfig {
    @Autowired
    private TestCouponDecreaseService testCouponDecreaseService;
    @Autowired
    private TestCouponRepository testCouponRepository;
    private TestCoupon coupon;

    @BeforeEach
    void setUp() {
        coupon = new TestCoupon("COUPON_001", 300L);
        testCouponRepository.save(coupon);
    }

    @Test
    @Order(1)
    void 쿠폰차감_분산락_적용_동시성_300명_테스트() throws InterruptedException {
        // given
        int threadCount = 300;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    testCouponDecreaseService.decreaseStockWithLock(coupon.getId(), "COUPON_001");
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        TestCoupon persistedCoupon = testCouponRepository.findById(coupon.getId()).orElseThrow(IllegalArgumentException::new);
        assertThat(persistedCoupon.getAvailableStock()).isZero();
        log.debug("잔여 쿠폰 수량: " + persistedCoupon.getAvailableStock());
    }

    @Test
    @Order(2)
    void 쿠폰차감_분산락_미적용_동시성_300명_테스트() throws InterruptedException {
        // given
        int threadCount = 300;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    testCouponDecreaseService.decreaseStock(coupon.getId());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        TestCoupon persistedCoupon = testCouponRepository.findById(coupon.getId()).orElseThrow(IllegalArgumentException::new);
        log.debug("잔여 쿠폰 수량: " + persistedCoupon.getAvailableStock());
    }
}
