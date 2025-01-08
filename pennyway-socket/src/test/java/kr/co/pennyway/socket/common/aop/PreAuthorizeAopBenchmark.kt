package kr.co.pennyway.socket.common.aop;

import kr.co.pennyway.domain.domains.user.domain.NotifySetting
import kr.co.pennyway.domain.domains.user.domain.User
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility
import kr.co.pennyway.domain.domains.user.type.Role
import kr.co.pennyway.socket.common.annotation.PreAuthorize
import kr.co.pennyway.socket.common.aop.PreAuthorizer.Companion.authenticate
import kr.co.pennyway.socket.common.aop.PreAuthorizer.Companion.authorize
import kr.co.pennyway.socket.common.exception.PreAuthorizeErrorException
import kr.co.pennyway.socket.common.security.authenticate.UserPrincipal
import lombok.extern.slf4j.Slf4j
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.data.projection.SpelAwareProxyProjectionFactory
import org.springframework.test.util.ReflectionTestUtils
import java.security.Principal
import java.time.LocalDateTime

@Disabled
@Slf4j
@SpringBootTest(
    classes = [
        PreAuthorizer::class,
        PreAuthorizer.PreAuthorizeAdvice::class,
        PreAuthorizeAspect::class,
        AuthorizationBenchmark.BenchmarkConfig::class,
        SpelAwareProxyProjectionFactory::class
    ]
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthorizationBenchmark {
    companion object {
        private const val WARMUP_ITERATIONS = 1000
        private const val TEST_ITERATIONS = 10000

        private const val WARMUP_BATCH_SIZE = 10
        private const val WARMUP_BATCH_COUNT = 10
    }

    @TestConfiguration
    @EnableAspectJAutoProxy
    class BenchmarkConfig {
        @Bean
        fun mockManager() = MockManager()

        @Bean
        fun testService() = TestService()
    }

    @Autowired
    private lateinit var testService: TestService

    private lateinit var validPrincipal: UserPrincipal
    private lateinit var expiredPrincipal: UserPrincipal

    @BeforeAll
    fun setup() {
        // 테스트용 Principal 생성
        val user = createUser()
        validPrincipal = UserPrincipal.of(user, LocalDateTime.now().plusMinutes(30), "deviceId", "deviceName")
        expiredPrincipal = UserPrincipal.of(user, LocalDateTime.now().minusHours(1), "deviceId", "deviceName")

        // 워밍업
        repeat(WARMUP_ITERATIONS) {
            // 각 시나리오별 워밍업 호출
            authenticate(validPrincipal) { true }
            authorize(MockManager::class, "hasPermission", 1L) { true }
            authorize(MockManager::class, "hasComplexPermission", 1L, "READ", true) { true }
        }

        repeat(WARMUP_ITERATIONS) {
            runCatching {
                testService.simpleAuthCheck(validPrincipal)
                testService.simplePermissionCheck(1L)
                testService.complexPermissionCheck(1L, "READ", true)
            }
        }
    }

    @Test
    fun `웜업 성능 비교`() {
        // AOP 방식 웜업 성능 측정
        prepareForWarmupTest()
        measureWarmup("Spring AOP 방식") {
            testService.simpleAuthCheck(validPrincipal)
            testService.simplePermissionCheck(1L)
            testService.complexPermissionCheck(1L, "READ", true)
        }

        // Reflection 방식 웜업 성능 측정
        prepareForWarmupTest()
        measureWarmup("Reflection 방식") {
            authenticate(validPrincipal) { true }
            authorize(MockManager::class, "hasPermission", 1L) { true }
            authorize(MockManager::class, "hasComplexPermission", 1L, "READ", true) { true }
        }
    }

    @Test
    fun `벤치마크 - 단순 인증 체크`() {
        measureTime("Spring AOP 단순 인증 체크") {
            testService.simpleAuthCheck(validPrincipal)
        }

        measureTime("단순 인증 체크") {
            authenticate(validPrincipal) { true }
        }
    }

    @Test
    fun `벤치마크 - 만료된 인증 체크`() {
        measureTime("Spring AOP 만료된 인증 체크") {
            assertThrows<PreAuthorizeErrorException> {
                testService.simpleAuthCheck(expiredPrincipal)
            }
        }

        measureTime("만료된 인증 체크") {
            assertThrows<PreAuthorizeErrorException> {
                authenticate(expiredPrincipal) { true }
            }
        }
    }

    @Test
    fun `벤치마크 - 단순 인가 체크 (성공)`() {
        measureTime("Spring AOP 단순 인가 체크 (성공)") {
            testService.simplePermissionCheck(1L)
        }

        measureTime("단순 인가 체크 (성공)") {
            authorize(MockManager::class, "hasPermission", 1L) { true }
        }
    }

    @Test
    fun `벤치마크 - 단순 인가 체크 (실패)`() {
        measureTime("Spring AOP 단순 인가 체크 (실패)") {
            assertThrows<PreAuthorizeErrorException> {
                testService.simplePermissionCheck(2L)
            }
        }

        measureTime("단순 인가 체크 (실패)") {
            assertThrows<PreAuthorizeErrorException> {
                authorize(MockManager::class, "hasPermission", 2L) { true }
            }
        }
    }

    @Test
    fun `벤치마크 - 복잡한 인가 체크 (성공)`() {
        measureTime("Spring AOP 복잡한 인가 체크 (성공)") {
            testService.complexPermissionCheck(1L, "READ", true)
        }

        measureTime("복잡한 인가 체크 (성공)") {
            authorize(
                MockManager::class,
                "hasComplexPermission",
                1L, "READ", true
            ) { true }
        }
    }

    @Test
    fun `벤치마크 - 복잡한 인가 체크 (실패)`() {
        measureTime("Spring AOP 복잡한 인가 체크 (실패)") {
            assertThrows<PreAuthorizeErrorException> {
                testService.complexPermissionCheck(2L, "WRITE", false)
            }
        }

        measureTime("복잡한 인가 체크 (실패)") {
            assertThrows<PreAuthorizeErrorException> {
                authorize(
                    MockManager::class,
                    "hasComplexPermission",
                    2L, "WRITE", false
                ) { true }
            }
        }
    }

    @Test
    fun `벤치마크 - 인증 인가 복합 체크 (성공)`() {
        measureTime("Spring AOP 인증 인가 복합 체크 (성공)") {
            testService.compositeCheck(validPrincipal, 1L)
        }

        measureTime("인증 인가 복합 체크 (성공)") {
            authorize(
                validPrincipal,
                MockManager::class,
                "hasPermission",
                1L
            ) { true }
        }
    }

    @Test
    fun `벤치마크 - 인증 인가 복합 체크 (인증 실패)`() {
        measureTime("Spring AOP 인증 인가 복합 체크 (인증 실패)") {
            assertThrows<PreAuthorizeErrorException> {
                testService.compositeCheck(expiredPrincipal, 1L)
            }
        }

        measureTime("인증 인가 복합 체크 (인증 실패)") {
            assertThrows<PreAuthorizeErrorException> {
                authorize(
                    expiredPrincipal,
                    MockManager::class,
                    "hasPermission",
                    1L
                ) { true }
            }
        }
    }

    @Test
    fun `벤치마크 - 인증 인가 복합 체크 (인가 실패)`() {
        measureTime("Spring AOP 인증 인가 복합 체크 (인가 실패)") {
            assertThrows<PreAuthorizeErrorException> {
                testService.compositeCheck(validPrincipal, 2L)
            }
        }

        measureTime("인증 인가 복합 체크 (인가 실패)") {
            assertThrows<PreAuthorizeErrorException> {
                authorize(
                    validPrincipal,
                    MockManager::class,
                    "hasPermission",
                    2L
                ) { true }
            }
        }
    }

    private fun measureTime(testName: String, block: () -> Unit) {
        val times = mutableListOf<Long>()

        repeat(TEST_ITERATIONS) {
            val start = System.nanoTime()
            block()
            val end = System.nanoTime()
            times.add(end - start)
        }

        val avgTime = times.average()
        val p95Time = times.sorted()[((TEST_ITERATIONS * 0.95).toInt())]

        println(
            """
            |테스트: $testName
            |평균 실행 시간: ${avgTime / 1000} μs
            |95th percentile: ${p95Time / 1000} μs
            |===================================
        """.trimMargin()
        )
    }

    private fun prepareForWarmupTest() {
        System.gc()
        Thread.sleep(1000)  // GC 완료 대기
    }

    private fun measureWarmup(testName: String, block: () -> Unit) {
        val batches = mutableListOf<Long>()

        repeat(WARMUP_BATCH_COUNT) { batchIndex ->
            val batchTimes = mutableListOf<Long>()

            repeat(WARMUP_BATCH_SIZE) {
                val start = System.nanoTime()
                block()
                val end = System.nanoTime()
                batchTimes.add(end - start)
            }

            batches.add(batchTimes.average().toLong())

            println(
                """
                |$testName - 배치 ${batchIndex + 1}
                |평균 실행 시간: ${batches.last() / 1000} μs
                |누적 평균 시간: ${batches.average() / 1000} μs
                |=================
            """.trimMargin()
            )
        }
    }

    private fun createUser(): User {
        val user = User.builder()
            .name("test")
            .username("jayang")
            .notifySetting(NotifySetting.of(true, true, true))
            .role(Role.USER)
            .password("password")
            .phone("010-1234-5678")
            .profileVisibility(ProfileVisibility.PUBLIC)
            .build()

        ReflectionTestUtils.setField(user, "id", 1L)
        return user
    }

    @TestComponent("mockManager")
    class MockManager {
        fun hasPermission(userId: Long): Boolean = userId == 1L
        fun hasComplexPermission(userId: Long, action: String, enabled: Boolean): Boolean =
            userId == 1L && action == "READ" && enabled
    }

    @TestComponent
    class TestService {
        @PreAuthorize("#isAuthenticated(#principal)")
        fun simpleAuthCheck(principal: Principal): Boolean = true

        @PreAuthorize("@mockManager.hasPermission(#userId)")
        fun simplePermissionCheck(userId: Long): Boolean = true

        @PreAuthorize("@mockManager.hasComplexPermission(#userId, #action, #enabled)")
        fun complexPermissionCheck(userId: Long, action: String, enabled: Boolean): Boolean = true

        @PreAuthorize("#isAuthenticated(#principal) and @mockManager.hasPermission(#userId)")
        fun compositeCheck(principal: Principal, userId: Long): Boolean = true
    }
}