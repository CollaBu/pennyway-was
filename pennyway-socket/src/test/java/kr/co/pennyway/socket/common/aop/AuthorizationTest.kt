package kr.co.pennyway.socket.common.aop

import kr.co.pennyway.domain.domains.user.domain.NotifySetting
import kr.co.pennyway.domain.domains.user.domain.User
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility
import kr.co.pennyway.domain.domains.user.type.Role
import kr.co.pennyway.socket.common.aop.PreAuthorizer.Companion.authenticate
import kr.co.pennyway.socket.common.aop.PreAuthorizer.Companion.authorize
import kr.co.pennyway.socket.common.aop.PreAuthorizer.Companion.permitAll
import kr.co.pennyway.socket.common.exception.PreAuthorizeErrorCode
import kr.co.pennyway.socket.common.exception.PreAuthorizeErrorException
import kr.co.pennyway.socket.common.security.authenticate.UserPrincipal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.context.ApplicationContext
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime

@SpringBootTest(classes = [PreAuthorizer::class, PreAuthorizer.PreAuthorizeAdvice::class, ApplicationContext::class, MockManager::class])
class AuthorizationTest {
    @Autowired
    private lateinit var preAuthorizerAdvice: PreAuthorizer.PreAuthorizeAdvice

    @Autowired
    private val applicationContext: ApplicationContext? = null

    @Test
    fun `permitAll을 호출하면 언제나 정상적으로 진행된다`() {
        // when
        val result = permitAll { "some result" }

        // then
        assertEquals("some result", result)
    }

    @Test
    fun `인증된 사용자는 정상적으로 진행된다`() {
        // given
        val (user, userPrincipal) = createValidFixture()

        // when
        val result = authenticate(principal = userPrincipal) {
            "some result"
        }

        // then
        assertEquals("some result", result)
    }

    @Test
    fun `인증되지 않은 사용자는 UNAUTHENTICATED 예외가 발생한다`() {
        // given
        val (user, userPrincipal) = createExpiredFixture()

        // when & then
        assertThrows<PreAuthorizeErrorException> {
            authenticate(principal = userPrincipal) {}
        }.also { e ->
            assertEquals(PreAuthorizeErrorCode.UNAUTHENTICATED, e.errorCode)
        }
    }

    @Test
    fun `인가된 사용자는 정상적으로 진행된다`() {
        val result = authorize(MockManager::class, MockManager::execute.name) {
            "some result"
        }

        // then
        assertEquals("some result", result)
    }

    @Test
    fun `인가되지 않은 사용자는 FORBIDDEN 예외를 반환한다`() {
        // when & then
        assertThrows<PreAuthorizeErrorException> {
            authorize(MockManager::class, MockManager::executeFail.name) {}
        }.also { e ->
            assertEquals(PreAuthorizeErrorCode.FORBIDDEN, e.errorCode)
        }
    }

    @Test
    fun `인증되었으나, 1번 사용자가 아니라면 FORBIDDEN 예외를 반환한다`() {
        // given
        val (user, userPrincipal) = createValidFixture()

        // when & then
        assertThrows<PreAuthorizeErrorException> {
            authorize(userPrincipal, MockManager::class, MockManager::hasPermission.name, 2L) {}
        }.also { e ->
            assertEquals(PreAuthorizeErrorCode.FORBIDDEN, e.errorCode)
        }
    }

    private fun createValidFixture(): Pair<User, UserPrincipal> {
        val user = createUser()
        val userPrincipal = UserPrincipal.of(user, LocalDateTime.now().plusMinutes(30), "deviceId", "deviceName")

        return user and userPrincipal
    }

    private fun createExpiredFixture(): Pair<User, UserPrincipal> {
        val user = createUser()
        val userPrincipal = UserPrincipal.of(user, LocalDateTime.now().minusHours(1), "deviceId", "deviceName")

        return user and userPrincipal
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

    data class Pair<out A, out B>(
        val first: A,
        val second: B
    )

    private infix fun <A, B> A.and(value: B) = Pair(this, value)
}

@TestComponent
class MockManager {
    fun execute(): Boolean {
        println("인가된 사용자입니다.")
        return true
    }

    fun executeFail(): Boolean {
        println("인가되지 않은 사용자입니다.")
        return false
    }

    fun hasPermission(userId: Long): Boolean {
        println("userId: $userId")
        return userId == 1L
    }
}