package kr.co.pennyway.api.apis.users.usecase;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.pennyway.api.apis.users.helper.PasswordEncoderHelper;
import kr.co.pennyway.api.apis.users.service.UserDeleteService;
import kr.co.pennyway.api.apis.users.service.UserProfileUpdateService;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.fixture.DeviceTokenFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.config.JpaConfig;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorCode;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorException;
import kr.co.pennyway.domain.domains.device.service.DeviceTokenService;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.service.OauthService;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import kr.co.pennyway.domain.domains.user.type.Role;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.AssertionErrors.*;

@ExtendWith(MockitoExtension.class)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@ContextConfiguration(classes = {
        JpaConfig.class, UserAccountUseCase.class, UserProfileUpdateService.class, UserDeleteService.class,
        UserService.class, DeviceTokenService.class, OauthService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class UserAccountUseCaseTest extends ExternalApiDBTestConfig {
    @Autowired
    private UserService userService;

    @Autowired
    private DeviceTokenService deviceTokenService;

    @Autowired
    private OauthService oauthService;

    @Autowired
    private UserAccountUseCase userAccountUseCase;

    @MockBean
    private PasswordEncoderHelper passwordEncoderHelper;

    @MockBean
    private JPAQueryFactory queryFactory;

    @Order(2)
    @Nested
    @DisplayName("[2] 디바이스 삭제 테스트")
    class DeviceTokenUnregisterTest {
        private User requestUser;

        @BeforeEach
        void setUp() {
            User user = User.builder().role(Role.USER).profileVisibility(ProfileVisibility.PUBLIC).build();
            requestUser = userService.createUser(user);
        }

        @Test
        @Transactional
        @DisplayName("사용자 ID와 origin token에 매칭되는 활성 디바이스가 존재하는 경우 디바이스를 삭제한다.")
        void unregisterDevice() {
            // given
            DeviceToken deviceToken = DeviceTokenFixture.INIT.toDevice(requestUser);
            deviceTokenService.createDevice(deviceToken);

            // when
            userAccountUseCase.unregisterDeviceToken(requestUser.getId(), deviceToken.getToken());

            // then
            Optional<DeviceToken> deletedDevice = deviceTokenService.readDeviceByUserIdAndToken(requestUser.getId(), deviceToken.getToken());
            assertNull("디바이스가 삭제되어 있어야 한다.", deletedDevice.orElse(null));
        }

        @Test
        @Transactional
        @DisplayName("사용자 ID와 token에 매칭되는 디바이스가 존재하지 않는 경우 NOT_FOUND_DEVICE 에러를 반환한다.")
        void unregisterDeviceWhenDeviceIsNotExists() {
            // given
            DeviceToken deviceToken = DeviceTokenFixture.INIT.toDevice(requestUser);
            deviceTokenService.createDevice(deviceToken);

            // when - then
            DeviceTokenErrorException ex = assertThrows(DeviceTokenErrorException.class, () -> userAccountUseCase.unregisterDeviceToken(requestUser.getId(), "notExistsToken"));
            assertEquals("디바이스 토큰이 존재하지 않으면 Not Found를 반환한다.", DeviceTokenErrorCode.NOT_FOUND_DEVICE, ex.getBaseErrorCode());
        }
    }

    @Order(3)
    @Nested
    @DisplayName("[3] 사용자 이름 수정 테스트")
    class UpdateNameTest {
        @Test
        @Transactional
        @DisplayName("사용자가 삭제된 유저인 경우 NOT_FOUND 에러를 반환한다.")
        void updateNameWhenUserIsDeleted() {
            // given
            String newName = "양재서";
            User originUser = UserFixture.GENERAL_USER.toUser();
            userService.createUser(originUser);
            userService.deleteUser(originUser);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> userAccountUseCase.updateName(originUser.getId(), newName));
            assertEquals("삭제된 사용자인 경우 Not Found를 반환한다.", UserErrorCode.NOT_FOUND, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("사용자의 이름이 성공적으로 변경된다.")
        void updateName() {
            // given
            User originUser = UserFixture.GENERAL_USER.toUser();
            userService.createUser(originUser);
            String newName = "양재서";

            // when
            userAccountUseCase.updateName(originUser.getId(), newName);

            // then
            User updatedUser = userService.readUser(originUser.getId()).orElseThrow();
            assertEquals("사용자 이름이 변경되어 있어야 한다.", newName, updatedUser.getName());
        }
    }

    @Order(4)
    @Nested
    @DisplayName("[4] 사용자 비밀번호 검증 테스트")
    class VerificationPasswordTest {
        private User originUser;

        @BeforeEach
        void setUp() {
            originUser = UserFixture.GENERAL_USER.toUser();
            userService.createUser(originUser);
        }

        @Test
        @Transactional
        @DisplayName("사용자가 삭제된 유저인 경우 NOT_FOUND 에러를 반환한다.")
        void verifyPasswordWhenUserIsDeleted() {
            // given
            userService.deleteUser(originUser);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> userAccountUseCase.verifyPassword(originUser.getId(), originUser.getPassword()));
            assertEquals("삭제된 사용자인 경우 Not Found를 반환한다.", UserErrorCode.NOT_FOUND, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("사용자가 일반 회원가입 이력이 없는 소셜 계정인 경우, DO_NOT_GENERAL_SIGNED_UP 에러를 반환한다.")
        void verifyPasswordWhenUserIsNotGeneralSignedUp() {
            // given
            User originUser = UserFixture.OAUTH_USER.toUser();
            userService.createUser(originUser);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> userAccountUseCase.verifyPassword(originUser.getId(), originUser.getPassword()));
            assertEquals("일반 회원가입 이력이 없는 경우 Do Not General Signed Up을 반환한다.", UserErrorCode.DO_NOT_GENERAL_SIGNED_UP, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("비밀번호가 다른 경우 NOT_MATCHED_PASSWORD 에러를 반환한다.")
        void verifyPasswordWhenPasswordIsNotMatched() {
            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> userAccountUseCase.verifyPassword(originUser.getId(), "notMatchedPassword"));
            assertEquals("비밀번호가 다른 경우 Not Matched Password를 반환한다.", UserErrorCode.NOT_MATCHED_PASSWORD, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("비밀번호가 일치하는 경우 정상적으로 처리된다.")
        void verifyPassword() {
            // given
            given(passwordEncoderHelper.isSamePassword(any(), any())).willReturn(true);

            // when - then
            assertDoesNotThrow(() -> userAccountUseCase.verifyPassword(originUser.getId(), originUser.getPassword()));
        }
    }

    @Order(5)
    @Nested
    @DisplayName("[5] 사용자 비밀번호 변경 테스트")
    class UpdatePasswordTest {
        private User originUser;

        @BeforeEach
        void setUp() {
            originUser = UserFixture.GENERAL_USER.toUser();
            userService.createUser(originUser);
        }

        @Test
        @Transactional
        @DisplayName("사용자가 삭제된 유저인 경우 NOT_FOUND 에러를 반환한다.")
        void updatePasswordWhenUserIsDeleted() {
            // given
            userService.deleteUser(originUser);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> userAccountUseCase.updatePassword(originUser.getId(), originUser.getPassword(), "newPassword"));
            assertEquals("삭제된 사용자인 경우 Not Found를 반환한다.", UserErrorCode.NOT_FOUND, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("oldPassword와 newPassword가 일치하는 경우와 현재 비밀번호와 동일한 비밀번호로 변경을 시도하는 경우, CLIENT_ERROR 에러를 반환한다.")
        void updatePasswordWhenSamePassword() {
            // given
            given(passwordEncoderHelper.isSamePassword(any(), any())).willReturn(true);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> userAccountUseCase.updatePassword(originUser.getId(), originUser.getPassword(), originUser.getPassword()));
            assertEquals("현재 비밀번호와 동일한 비밀번호로 변경할 수 없는 경우 Client Error를 반환한다.", UserErrorCode.PASSWORD_NOT_CHANGED, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("비밀번호가 다른 경우 NOT_MATCHED_PASSWORD 에러를 반환한다.")
        void updatePasswordWhenPasswordIsNotMatched() {
            // given
            given(passwordEncoderHelper.isSamePassword(any(), any())).willReturn(false);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> userAccountUseCase.updatePassword(originUser.getId(), "notMatchedPassword", "newPassword"));
            assertEquals("비밀번호가 다른 경우 Not Matched Password를 반환한다.", UserErrorCode.NOT_MATCHED_PASSWORD, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("사용자가 일반 회원가입 이력이 없는 소셜 계정인 경우, DO_NOT_GENERAL_SIGNED_UP 에러를 반환한다.")
        void updatePasswordWhenUserIsNotGeneralSignedUp() {
            // given
            User originUser = UserFixture.OAUTH_USER.toUser();
            userService.createUser(originUser);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> userAccountUseCase.updatePassword(originUser.getId(), originUser.getPassword(), "newPassword"));
            assertEquals("일반 회원가입 이력이 없는 경우 Do Not General Signed Up을 반환한다.", UserErrorCode.DO_NOT_GENERAL_SIGNED_UP, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("정상적인 요청인 경우 비밀번호가 정상적으로 변경된다.")
        void updatePassword() {
            // given
            given(passwordEncoderHelper.isSamePassword(originUser.getPassword(), originUser.getPassword())).willReturn(true);
            given(passwordEncoderHelper.isSamePassword(originUser.getPassword(), "newPassword")).willReturn(false);
            given(passwordEncoderHelper.encodePassword(any())).willReturn("encodedPassword");

            // when - then
            assertDoesNotThrow(() -> userAccountUseCase.updatePassword(originUser.getId(), originUser.getPassword(), "newPassword"));
            assertEquals("비밀번호가 정상적으로 변경되어 있어야 한다.", "encodedPassword", userService.readUser(originUser.getId()).orElseThrow().getPassword());
        }
    }

    @Order(6)
    @Nested
    @DisplayName("[6] 사용자 계정 삭제")
    class DeleteAccountTest {
        @Test
        @Transactional
        @DisplayName("사용자가 삭제된 유저를 조회하려는 경우 NOT_FOUND 에러를 반환한다.")
        void deleteAccountWhenUserIsDeleted() {
            // given
            User user = UserFixture.GENERAL_USER.toUser();
            userService.createUser(user);
            userService.deleteUser(user);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> userAccountUseCase.deleteAccount(user.getId()));
            assertEquals("삭제된 사용자인 경우 Not Found를 반환한다.", UserErrorCode.NOT_FOUND, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("일반 회원가입 이력만 있는 사용자의 경우, 정상적으로 계정이 삭제된다.")
        void deleteAccount() {
            // given
            User user = UserFixture.GENERAL_USER.toUser();
            userService.createUser(user);

            // when - then
            assertDoesNotThrow(() -> userAccountUseCase.deleteAccount(user.getId()));
            assertTrue("사용자가 삭제되어 있어야 한다.", userService.readUser(user.getId()).isEmpty());
        }

        @Test
        @Transactional
        @DisplayName("사용자 계정 삭제 시, 연동된 모든 소셜 계정은 soft delete 처리되어야 한다.")
        void deleteAccountWithSocialAccounts() {
            // given
            User user = UserFixture.OAUTH_USER.toUser();
            userService.createUser(user);

            Oauth kakao = createOauth(Provider.KAKAO, "kakaoId", user);
            Oauth google = createOauth(Provider.GOOGLE, "googleId", user);

            // when - then
            assertDoesNotThrow(() -> userAccountUseCase.deleteAccount(user.getId()));
            assertTrue("사용자가 삭제되어 있어야 한다.", userService.readUser(user.getId()).isEmpty());
            assertTrue("카카오 계정이 삭제되어 있어야 한다.", oauthService.readOauth(kakao.getId()).get().isDeleted());
            assertTrue("구글 계정이 삭제되어 있어야 한다.", oauthService.readOauth(google.getId()).get().isDeleted());
        }

        @Test
        @Transactional
        @DisplayName("사용자 삭제 시, 디바이스 정보는 CASCADE로 삭제되어야 한다.")
        void deleteAccountWithDevices() {
            // given
            User user = UserFixture.GENERAL_USER.toUser();
            userService.createUser(user);

            DeviceToken deviceToken = DeviceTokenFixture.INIT.toDevice(user);
            deviceTokenService.createDevice(deviceToken);

            // when - then
            assertDoesNotThrow(() -> userAccountUseCase.deleteAccount(user.getId()));
            assertTrue("사용자가 삭제되어 있어야 한다.", userService.readUser(user.getId()).isEmpty());
            assertTrue("디바이스가 삭제되어 있어야 한다.", deviceTokenService.readDeviceByUserIdAndToken(user.getId(), deviceToken.getToken()).isEmpty());
        }

        private Oauth createOauth(Provider provider, String providerId, User user) {
            Oauth oauth = Oauth.of(provider, providerId, user);
            return oauthService.createOauth(oauth);
        }
    }
}