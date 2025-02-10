package kr.co.pennyway.api.apis.auth.service;

import kr.co.pennyway.api.apis.auth.dto.AuthFindDto;
import kr.co.pennyway.api.apis.users.helper.PasswordEncoderHelper;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.context.account.service.UserService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AuthFindServiceTest {
    private AuthFindService authFindService;
    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoderHelper passwordEncoderHelper;

    @BeforeEach
    void setUp() {
        authFindService = new AuthFindService(userService, passwordEncoderHelper);
    }

    @DisplayName("휴대폰 번호로 유저를 찾을 수 없을 때 AuthFinderException을 발생시킨다.")
    @Test
    void findUsernameIfUserNotFound() {
        // given
        String phone = "010-1234-5678";
        given(userService.readUserByPhone(phone)).willReturn(Optional.empty());

        // when - then
        UserErrorException exception = assertThrows(UserErrorException.class, () -> authFindService.findUsername(phone));
        log.debug(exception.getExplainError());
    }

    @DisplayName("휴대폰 번호로 유저를 찾았으나 OAuth 유저일 때 AuthFinderException을 발생시킨다.")
    @Test
    void findUsernameIfUserIsOAuth() {
        // given
        String phone = "010-1234-5678";
        User user = UserFixture.OAUTH_USER.toUser();
        given(userService.readUserByPhone(phone)).willReturn(Optional.of(user));

        // when - then
        UserErrorException exception = assertThrows(UserErrorException.class, () -> authFindService.findUsername(phone));
        log.debug(exception.getExplainError());
    }

    @DisplayName("휴대폰 번호를 통해 유저를 찾아 User를 반환한다.")
    @Test
    void findUsernameIfUserFound() {
        // given
        String phone = "010-1234-5678";
        String username = "jayang";
        User user = UserFixture.GENERAL_USER.toUser();
        given(userService.readUserByPhone(phone)).willReturn(Optional.of(user));

        // when
        AuthFindDto.FindUsernameRes result = authFindService.findUsername(phone);

        // then
        assertEquals(result, new AuthFindDto.FindUsernameRes(username));
    }


    // BestPractice

    // 없는 유저 폰번호 쐈을때
    @DisplayName("존재하지 않는 사용자의 번호로 비밀번호 찾기 인증요청이 올 경우 UserErrorException을 발생시킨다.")
    @Test
    void findPasswordVerificationIfUserNotFound() {
        // given
        String phone = "010-1234-5678";
        given(userService.readUserByPhone(phone)).willReturn(Optional.empty());

        // when - then
        assertThrows(UserErrorException.class, () -> authFindService.existsGeneralSignUpUser(phone));
    }

    // Oauth 유저로 인증 쐈을때
    @DisplayName("Oauth 유저의 번호로 비밀번호 찾기 인증요청이 올 경우 UserErrorException을 발생시킨다.")
    @Test
    void findPasswordVerificationIfUserOauth() {
        // given
        String phone = "010-1234-5678";
        User user = UserFixture.OAUTH_USER.toUser();
        given(userService.readUserByPhone(phone)).willReturn(Optional.of(user));

        // when - then
        assertThrows(UserErrorException.class, () -> authFindService.existsGeneralSignUpUser(phone));
    }

    @DisplayName("정상적인 비밀번호 찾기 인증요청일 경우 SuccessResponse.noContent()를 반환한다.")
    @Test
    void findPasswordVerification() {
        // given
        String phone = "010-1234-5678";
        User user = UserFixture.GENERAL_USER.toUser();
        given(userService.readUserByPhone(phone)).willReturn(Optional.of(user));

        // when
        authFindService.existsGeneralSignUpUser(phone);
    }

    @DisplayName("존재하지 않는 사용자의 번호로 비밀번호 변경 요청이 올 경우 UserErrorException을 발생시킨다.")
    @Test
    void updatePasswordIfUserNotFound() {
        // given
        String phone = "010-1234-5678";
        String newPassword = "newPassword123";
        given(userService.readUserByPhone(phone)).willReturn(Optional.empty());

        // when - then
        assertThrows(UserErrorException.class, () -> authFindService.updatePassword(phone, newPassword));
    }

    @DisplayName("정상적인 요청일 경우 비밀번호를 변경한다.")
    @Test
    void updatePassword() {
        // given
        String phone = "010-1234-5678";
        String newPassword = "newPassword123";
        User user = UserFixture.GENERAL_USER.toUser();
        given(userService.readUserByPhone(phone)).willReturn(Optional.of(user));
        given(passwordEncoderHelper.encodePassword(newPassword)).willReturn("encodedNewPassword");

        // when
        authFindService.updatePassword(phone, newPassword);

        // then
        assertEquals("encodedNewPassword", user.getPassword());
    }
}