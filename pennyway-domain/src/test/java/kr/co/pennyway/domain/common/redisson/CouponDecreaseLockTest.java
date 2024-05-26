package kr.co.pennyway.domain.common.redisson;

import jakarta.persistence.EntityManager;
import kr.co.pennyway.domain.common.aop.DistributedLockAop;
import kr.co.pennyway.domain.common.aop.RedissonCallNewTransaction;
import kr.co.pennyway.domain.config.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles("test")
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JpaConfig.class, RedisConfig.class, RedissonConfig.class, RedissonCallNewTransaction.class})
@EnableAspectJAutoProxy
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import({TestJpaConfig.class})
public class CouponDecreaseLockTest extends ContainerDBTestConfig {
    @Autowired
    private TestEntityManager testEntityManager;
    private EntityManager em;
    private TestCouponDecreaseService testCouponDecreaseService;
    private TestCoupon coupon;

    @BeforeEach
    void setUp(AnnotationConfigApplicationContext context) {
        context.register(DistributedLockAop.class);

        em = testEntityManager.getEntityManager();
        testCouponDecreaseService = new TestCouponDecreaseService(em);

        coupon = new TestCoupon("COUPON_001", 300L);
        em.persist(coupon);
        em.flush();
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
