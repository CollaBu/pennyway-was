package kr.co.pennyway.domain.common.redisson;

import jakarta.persistence.EntityManager;
import kr.co.pennyway.domain.config.ContainerDBTestConfig;
import kr.co.pennyway.domain.config.DomainIntegrationTest;
import kr.co.pennyway.domain.domains.coupon.TestCoupon;
import kr.co.pennyway.domain.domains.coupon.TestCouponDecreaseService;
import kr.co.pennyway.domain.domains.coupon.TestCouponRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DomainIntegrationTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EntityScan(basePackageClasses = {TestCoupon.class})
public class CouponDecreaseLockTest extends ContainerDBTestConfig {
    @Autowired
    private EntityManager em;
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
    @Transactional
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
        TestCoupon persistedCoupon = em.find(TestCoupon.class, coupon.getId());
        assertThat(persistedCoupon.getAvailableStock()).isZero();
        log.debug("잔여 쿠폰 수량: " + persistedCoupon.getAvailableStock());
    }

//    @Test
//    @Order(2)
//    void 쿠폰차감_분산락_미적용_동시성_100명_테스트() throws InterruptedException {
//        // given
//        int threadCount = 100;
//        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//        CountDownLatch latch = new CountDownLatch(threadCount);
//
//        // when
//        for (int i = 0; i < threadCount; i++) {
//            executorService.submit(() -> {
//                try {
//                    testCouponDecreaseService.decreaseStock(coupon.getId());
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//        latch.await();
//
//        // then
//        TestCoupon persistedCoupon = em.find(TestCoupon.class, coupon.getId());
//        log.debug("잔여 쿠폰 수량: " + persistedCoupon.getAvailableStock());
//    }
}
