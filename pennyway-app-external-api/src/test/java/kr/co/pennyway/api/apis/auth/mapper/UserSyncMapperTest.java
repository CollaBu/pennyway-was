package kr.co.pennyway.api.apis.auth.mapper;

import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserSyncMapperTest {
    private final String phone = "010-1234-5678";
    private UserSyncMapper userSyncMapper;
    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        userSyncMapper = new UserSyncMapper(userService);
    }

    @DisplayName("일반 회원가입 시, 회원 정보가 없으면 FALSE를 반환한다.")
    @Test
    void isSignedUserWhenGeneralReturnFalse() {
        // given
        given(userService.readUserByPhone(phone)).willReturn(Optional.empty());

        // when
        Boolean result = userSyncMapper.isGeneralSignUpAllowed(phone).getKey();

        // then
        assertEquals(result, Boolean.FALSE);
    }

    @DisplayName("일반 회원가입 시, oauth 회원 정보가 있으면 TRUE를 반환한다.")
    @Test
    void isSignedUserWhenGeneralReturnTrue() {
        // given
        given(userService.readUserByPhone(phone)).willReturn(Optional.of(User.builder().username("pennyway").password(null).build()));

        // when
        Pair<Boolean, String> result = userSyncMapper.isGeneralSignUpAllowed(phone);

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
        assertNull(userSyncMapper.isGeneralSignUpAllowed(phone));
    }
}
