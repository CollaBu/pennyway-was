package kr.co.pennyway.api.apis.auth.service;

import kr.co.pennyway.api.apis.auth.dto.UserSyncDto;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserGeneralSignServiceTest {
    private final String phone = "010-1234-5678";
    private UserGeneralSignService userGeneralSignService;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userGeneralSignService = new UserGeneralSignService(userService, passwordEncoder);
    }

    @DisplayName("일반 회원가입 시, 회원 정보가 없으면 {회원 가입 가능, 기존 계정 없음} 응답을 반환한다.")
    @Test
    void isSignedUserWhenGeneralReturnFalse() {
        // given
        given(userService.readUserByPhone(phone)).willReturn(Optional.empty());

        // when
        UserSyncDto userSync = userGeneralSignService.isSignUpAllowed(phone);

        // then
        assertTrue(userSync.isSignUpAllowed());
        assertFalse(userSync.isExistAccount());
        assertNull(userSync.username());
    }

    @DisplayName("일반 회원가입 시, oauth 회원 정보만 있으면 {회원 가입 가능, 기존 계정 있음, 기존 계정 아이디} 응답을 반환한다.")
    @Test
    void isSignedUserWhenGeneralReturnTrue() {
        // given
        given(userService.readUserByPhone(phone)).willReturn(Optional.of(UserFixture.OAUTH_USER.toUser()));

        // when
        UserSyncDto userSync = userGeneralSignService.isSignUpAllowed(phone);

        // then
        assertTrue(userSync.isSignUpAllowed());
        assertTrue(userSync.isExistAccount());
        assertEquals(UserFixture.OAUTH_USER.getUsername(), userSync.username());
    }

    @DisplayName("일반 회원가입 시, 이미 일반회원 가입된 회원인 경우 계정 생성 불가 응답을 반환한다.")
    @Test
    void isSignedUserWhenGeneralThrowUserErrorException() {
        // given
        given(userService.readUserByPhone(phone)).willReturn(Optional.of(UserFixture.GENERAL_USER.toUser()));

        // when
        UserSyncDto userSync = userGeneralSignService.isSignUpAllowed(phone);

        // then
        assertFalse(userSync.isSignUpAllowed());
        assertTrue(userSync.isExistAccount());
        assertEquals(UserFixture.GENERAL_USER.getUsername(), userSync.username());
    }

    @DisplayName("로그인 시, 유저가 존재하고 비밀번호가 일치하면 User를 반환한다.")
    @Test
    void readUserIfValidReturnUser() {
        // given
        User user = UserFixture.GENERAL_USER.toUser();
        given(userService.readUserByUsername(user.getUsername())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(user.getPassword(), user.getPassword())).willReturn(true);

        // when
        User result = userGeneralSignService.readUserIfValid(user.getUsername(), user.getPassword());

        // then
        assertEquals(result, user);
    }

    @DisplayName("로그인 시, username에 해당하는 유저가 존재하지 않으면 UserErrorException을 발생시킨다.")
    @Test
    void readUserIfNotFound() {
        // given
        given(userService.readUserByUsername("pennyway")).willThrow(new UserErrorException(UserErrorCode.NOT_FOUND));

        // when - then
        UserErrorException exception = assertThrows(UserErrorException.class, () -> userGeneralSignService.readUserIfValid("pennyway", "password"));
        System.out.println(exception.getExplainError());
    }

    @DisplayName("로그인 시, 비밀번호가 일치하지 않으면 UserErrorException을 발생시킨다.")
    @Test
    void readUserIfNotMatchedPassword() {
        // given
        User user = UserFixture.GENERAL_USER.toUser();
        given(userService.readUserByUsername(any())).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password", user.getPassword())).willReturn(false);

        // when - then
        UserErrorException exception = assertThrows(UserErrorException.class, () -> userGeneralSignService.readUserIfValid("pennyway", "password"));
        System.out.println(exception.getExplainError());
    }
}
