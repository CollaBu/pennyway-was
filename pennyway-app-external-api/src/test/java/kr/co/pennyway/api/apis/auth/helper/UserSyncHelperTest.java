package kr.co.pennyway.api.apis.auth.helper;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserSyncHelperTest {
    private final String phone = "010-1234-5678";
    private UserSyncHelper userSyncHelper;
    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        userSyncHelper = new UserSyncHelper(userService);
    }

    @DisplayName("일반 회원가입 시, 회원 정보가 없으면 oauth == false, username == null을 반환한다.")
    @Test
    void isSignedUserWhenGeneralReturnFalse() {
        // given
        given(userService.readUserByPhone(phone)).willThrow(new UserErrorException(UserErrorCode.NOT_FOUND));

        // when
        Pair<Boolean, String> result = userSyncHelper.isSignedUserWhenGeneral(phone);

        // then
        assertEquals(result.getKey(), Boolean.FALSE);
        assertNull(result.getValue());
    }

    @DisplayName("일반 회원가입 시, oauth 회원 정보가 있으면 oauth == true, username을 반환한다.")
    @Test
    void isSignedUserWhenGeneralReturnTrue() {
        // given
        given(userService.readUserByPhone(phone)).willReturn(User.builder().username("pennyway").password(null).build());

        // when
        Pair<Boolean, String> result = userSyncHelper.isSignedUserWhenGeneral(phone);

        // then
        assertEquals(result.getKey(), Boolean.TRUE);
        assertEquals(result.getValue(), "pennyway");
    }

    @DisplayName("일반 회원가입 시, 이미 일반회원 가입된 회원인 경우 UserErrorException을 발생시킨다.")
    @Test
    void isSignedUserWhenGeneralThrowUserErrorException() {
        // given
        given(userService.readUserByPhone(phone)).willReturn(User.builder().password("password").build());

        // when - then
        UserErrorException exception = org.junit.jupiter.api.Assertions.assertThrows(UserErrorException.class, () -> userSyncHelper.isSignedUserWhenGeneral(phone));
        System.out.println(exception.getExplainError());
    }
}
