package kr.co.pennyway.api.apis.users.service;

import kr.co.pennyway.api.apis.auth.service.PhoneVerificationService;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.common.redis.phone.PhoneCodeService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class UserProfileUpdateServiceTest {
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
    @DisplayName("수정 요청한 아이디와 전화번호가 기존 정보와 일치할 경우, 변경이 발생하지 않는다.")
    void updateSameUsermameAndPhone() {
        // when
        userProfileUpdateService.updateUsernameAndPhone(userId, user.getUsername(), user.getPhone(), "000000");

        // then
        verifyNoInteractions(awsS3Provider, phoneVerificationService, phoneCodeService);
    }

    @Test
    @DisplayName("수정 요청한 아이디만 기존 정보와 다를 경우, 아이디만 변경이 발생한다.")
    void updateDifferentUsername() {
        // given
        String newUsername = "newUsername";
        String expectedPhone = user.getPhone();

        // when
        userProfileUpdateService.updateUsernameAndPhone(userId, newUsername, user.getPhone(), "000000");

        // then
        assertEquals(newUsername, user.getUsername());
        assertEquals(expectedPhone, user.getPhone());
        verifyNoInteractions(awsS3Provider, phoneVerificationService, phoneCodeService);
    }
}
