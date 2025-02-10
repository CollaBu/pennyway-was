package kr.co.pennyway.api.apis.users.service;

import kr.co.pennyway.api.apis.auth.service.PhoneVerificationService;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.context.account.service.PhoneCodeService;
import kr.co.pennyway.domain.context.account.service.UserService;
import kr.co.pennyway.domain.domains.phone.type.PhoneCodeKeyType;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.infra.client.aws.s3.AwsS3Provider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verifyNoInteractions;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class PhoneUpdateServiceTest {
    private final Long userId = 1L;
    private User user = UserFixture.GENERAL_USER.toUser();
    @InjectMocks
    private UserProfileUpdateService userProfileUpdateService;
    @Mock
    private UserService userService;
    @Mock
    private AwsS3Provider awsS3Provider;
    @Mock
    private PhoneVerificationService phoneVerificationService;
    @Mock
    private PhoneCodeService phoneCodeService;

    @BeforeEach
    void setUp() {
        given(userService.readUser(userId)).willReturn(Optional.of(user));
    }

    @Test
    @DisplayName("수정 요청한 전화번호가 DB에 존재하지 않고, 유효한 인증 코드를 가진 경우 수정에 성공한다.")
    void updateDifferentPhone() {
        // given
        String expectedUsername = user.getUsername();
        String newPhone = "010-0000-0000";
        given(phoneVerificationService.isValidCode(any(), eq(PhoneCodeKeyType.PHONE))).willReturn(true);
        willDoNothing().given(phoneCodeService).delete(newPhone, PhoneCodeKeyType.PHONE);

        // when
        userProfileUpdateService.updatePhone(userId, newPhone, "000000");

        // then
        assertEquals(expectedUsername, user.getUsername());
        assertEquals(newPhone, user.getPhone());
        verifyNoInteractions(awsS3Provider);
    }

    @Test
    @DisplayName("수정 요청한 전화번호가 이미 존재하면, ALREADY_EXIST_PHONE 에러를 반환한다.")
    void updateAlreadyExistPhone() {
        // given
        String newPhone = "010-0000-0000";
        given(userService.isExistPhone(newPhone)).willReturn(true);
        given(phoneVerificationService.isValidCode(any(), eq(PhoneCodeKeyType.PHONE))).willReturn(true);
        willDoNothing().given(phoneCodeService).delete(newPhone, PhoneCodeKeyType.PHONE);

        // when
        UserErrorException exception = assertThrows(UserErrorException.class, () -> userProfileUpdateService.updatePhone(userId, newPhone, "000000"));

        // then
        assertEquals(UserErrorCode.ALREADY_EXIST_PHONE, exception.getBaseErrorCode());
        verifyNoInteractions(awsS3Provider);
    }
}
