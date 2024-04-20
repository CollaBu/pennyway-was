package kr.co.pennyway.api.apis.auth.service;

import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @DisplayName("일반 회원가입 시, 회원 정보가 없으면 FALSE를 반환한다.")
    @Test
    void isSignedUserWhenGeneralReturnFalse() {
        // given
        given(userService.readUserByPhone(phone)).willReturn(Optional.empty());

        // when
        Boolean result = userGeneralSignService.isSignUpAllowed(phone).getKey();

        // then
        assertEquals(result, Boolean.FALSE);
    }

    @DisplayName("일반 회원가입 시, oauth 회원 정보가 있으면 TRUE를 반환한다.")
    @Test
    void isSignedUserWhenGeneralReturnTrue() {
        // given
        given(userService.readUserByPhone(phone)).willReturn(Optional.of(User.builder().username("pennyway").password(null).build()));

        // when
        Pair<Boolean, String> result = userGeneralSignService.isSignUpAllowed(phone);

        // then
        assertEquals(result.getLeft(), Boolean.TRUE);
        assertEquals(result.getRight(), "pennyway");
    }

    @DisplayName("일반 회원가입 시, 이미 일반회원 가입된 회원인 경우 null을 반환한다.")
    @Test
    void isSignedUserWhenGeneralThrowUserErrorException() {
        // given
        given(userService.readUserByPhone(phone)).willReturn(
                Optional.of(User.builder().password("password").build()));

        // when - then
        assertNull(userGeneralSignService.isSignUpAllowed(phone));
    }

    @DisplayName("로그인 시, 유저가 존재하고 비밀번호가 일치하면 User를 반환한다.")
    @Test
    void readUserIfValidReturnUser() {
        // given
        User user = User.builder().username("pennyway").password("password").build();
        given(userService.readUserByUsername("pennyway")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password", user.getPassword())).willReturn(true);

        // when
        User result = userGeneralSignService.readUserIfValid("pennyway", "password");

        // then
        assertEquals(result, user);
    }

    @DisplayName("로그인 시, username에 해당하는 유저가 존재하지 않으면 UserErrorException을 발생시킨다.")
    @Test
    void readUserIfNotFound() {
        // given
        given(userService.readUserByUsername("pennyway")).willThrow(
                new UserErrorException(UserErrorCode.NOT_FOUND));

        // when - then
        UserErrorException exception = assertThrows(UserErrorException.class, () -> userGeneralSignService.readUserIfValid("pennyway", "password"));
        System.out.println(exception.getExplainError());
    }

    @DisplayName("로그인 시, 비밀번호가 일치하지 않으면 UserErrorException을 발생시킨다.")
    @Test
    void readUserIfNotMatchedPassword() {
        // given
        User user = User.builder().username("pennyway").password("password").build();
        given(userService.readUserByUsername("pennyway")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password", user.getPassword())).willReturn(false);

        // when - then
        UserErrorException exception = assertThrows(UserErrorException.class, () -> userGeneralSignService.readUserIfValid("pennyway", "password"));
        System.out.println(exception.getExplainError());
    }
}
