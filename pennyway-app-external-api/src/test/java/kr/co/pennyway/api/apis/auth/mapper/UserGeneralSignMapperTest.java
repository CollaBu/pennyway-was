package kr.co.pennyway.api.apis.auth.mapper;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserGeneralSignMapperTest {
    private UserGeneralSignMapper userGeneralSignMapper;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userGeneralSignMapper = new UserGeneralSignMapper(userService, passwordEncoder);
    }

    @DisplayName("로그인 시, 유저가 존재하고 비밀번호가 일치하면 User를 반환한다.")
    @Test
    void readUserIfValidReturnUser() {
        // given
        User user = User.builder().username("pennyway").password("password").build();
        given(userService.readUserByUsername("pennyway")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password", user.getPassword())).willReturn(true);

        // when
        User result = userGeneralSignMapper.readUserIfValid("pennyway", "password");

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
        UserErrorException exception = assertThrows(UserErrorException.class, () -> userGeneralSignMapper.readUserIfValid("pennyway", "password"));
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
        UserErrorException exception = assertThrows(UserErrorException.class, () -> userGeneralSignMapper.readUserIfValid("pennyway", "password"));
        System.out.println(exception.getExplainError());
    }
}
