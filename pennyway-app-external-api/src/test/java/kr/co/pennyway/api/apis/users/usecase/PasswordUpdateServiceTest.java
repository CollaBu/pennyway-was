package kr.co.pennyway.api.apis.users.usecase;

import kr.co.pennyway.api.apis.users.helper.PasswordEncoderHelper;
import kr.co.pennyway.api.apis.users.service.PasswordUpdateService;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.TestJpaConfig;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.config.JpaConfig;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(MockitoExtension.class)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@ContextConfiguration(classes = {JpaConfig.class, PasswordUpdateService.class, UserService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfig.class)
public class PasswordUpdateServiceTest extends ExternalApiDBTestConfig {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordUpdateService passwordUpdateService;

    @MockBean
    private PasswordEncoderHelper passwordEncoderHelper;

    @Nested
    @DisplayName("사용자 비밀번호 검증 테스트")
    class VerificationPasswordTest {
        private User originUser;

        @BeforeEach
        void setUp() {
            originUser = userService.createUser(UserFixture.GENERAL_USER.toUser());
        }

        @Test
        @Transactional
        @DisplayName("[1] 사용자가 삭제된 유저인 경우 NOT_FOUND 에러를 반환한다.")
        void verifyPasswordWhenUserIsDeleted() {
            // given
            userService.deleteUser(originUser);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> passwordUpdateService.verify(originUser.getId(), originUser.getPassword()));
            assertEquals("삭제된 사용자인 경우 Not Found를 반환한다.", UserErrorCode.NOT_FOUND, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("[2] 사용자가 일반 회원가입 이력이 없는 소셜 계정인 경우, DO_NOT_GENERAL_SIGNED_UP 에러를 반환한다.")
        void verifyPasswordWhenUserIsNotGeneralSignedUp() {
            // given
            User originUser = UserFixture.OAUTH_USER.toUser();
            userService.createUser(originUser);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> passwordUpdateService.verify(originUser.getId(), originUser.getPassword()));
            assertEquals("일반 회원가입 이력이 없는 경우 Do Not General Signed Up을 반환한다.", UserErrorCode.DO_NOT_GENERAL_SIGNED_UP, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("[3] 비밀번호가 다른 경우 NOT_MATCHED_PASSWORD 에러를 반환한다.")
        void verifyPasswordWhenPasswordIsNotMatched() {
            // given
            given(passwordEncoderHelper.isSamePassword(any(), any())).willReturn(false);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> passwordUpdateService.verify(originUser.getId(), "notMatchedPassword"));
            assertEquals("비밀번호가 다른 경우 Not Matched Password를 반환한다.", UserErrorCode.NOT_MATCHED_PASSWORD, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("[4] 비밀번호가 일치하는 경우 정상적으로 처리된다.")
        void verifyPassword() {
            // given
            given(passwordEncoderHelper.isSamePassword(any(), any())).willReturn(true);

            // when - then
            assertDoesNotThrow(() -> passwordUpdateService.verify(originUser.getId(), originUser.getPassword()));
        }
    }

    @Nested
    @DisplayName("사용자 비밀번호 변경 테스트")
    class UpdatePasswordTest {
        private User originUser;

        @BeforeEach
        void setUp() {
            originUser = UserFixture.GENERAL_USER.toUser();
            userService.createUser(originUser);
        }

        @Test
        @Transactional
        @DisplayName("[1] 사용자가 삭제된 유저인 경우 NOT_FOUND 에러를 반환한다.")
        void updatePasswordWhenUserIsDeleted() {
            // given
            userService.deleteUser(originUser);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> passwordUpdateService.execute(originUser.getId(), originUser.getPassword(), "newPassword"));
            assertEquals("삭제된 사용자인 경우 Not Found를 반환한다.", UserErrorCode.NOT_FOUND, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("[2] oldPassword와 newPassword가 일치하는 경우와 현재 비밀번호와 동일한 비밀번호로 변경을 시도하는 경우, CLIENT_ERROR 에러를 반환한다.")
        void updatePasswordWhenSamePassword() {
            // given
            given(passwordEncoderHelper.isSamePassword(any(), any())).willReturn(true);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> passwordUpdateService.execute(originUser.getId(), originUser.getPassword(), originUser.getPassword()));
            assertEquals("현재 비밀번호와 동일한 비밀번호로 변경할 수 없는 경우 Client Error를 반환한다.", UserErrorCode.PASSWORD_NOT_CHANGED, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("[3] 비밀번호가 다른 경우 NOT_MATCHED_PASSWORD 에러를 반환한다.")
        void updatePasswordWhenPasswordIsNotMatched() {
            // given
            given(passwordEncoderHelper.isSamePassword(any(), any())).willReturn(false);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> passwordUpdateService.execute(originUser.getId(), "notMatchedPassword", "newPassword"));
            assertEquals("비밀번호가 다른 경우 Not Matched Password를 반환한다.", UserErrorCode.NOT_MATCHED_PASSWORD, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("[4] 사용자가 일반 회원가입 이력이 없는 소셜 계정인 경우, DO_NOT_GENERAL_SIGNED_UP 에러를 반환한다.")
        void updatePasswordWhenUserIsNotGeneralSignedUp() {
            // given
            User originUser = userService.createUser(UserFixture.OAUTH_USER.toUser());

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> passwordUpdateService.execute(originUser.getId(), originUser.getPassword(), "newPassword"));
            assertEquals("일반 회원가입 이력이 없는 경우 Do Not General Signed Up을 반환한다.", UserErrorCode.DO_NOT_GENERAL_SIGNED_UP, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("[5] 정상적인 요청인 경우 비밀번호가 정상적으로 변경된다.")
        void updatePassword() {
            // given
            given(passwordEncoderHelper.isSamePassword(originUser.getPassword(), originUser.getPassword())).willReturn(true);
            given(passwordEncoderHelper.isSamePassword(originUser.getPassword(), "newPassword")).willReturn(false);
            given(passwordEncoderHelper.encodePassword(any())).willReturn("encodedPassword");

            // when - then
            assertDoesNotThrow(() -> passwordUpdateService.execute(originUser.getId(), originUser.getPassword(), "newPassword"));
            assertEquals("비밀번호가 정상적으로 변경되어 있어야 한다.", "encodedPassword", userService.readUser(originUser.getId()).orElseThrow().getPassword());
        }
    }
}
