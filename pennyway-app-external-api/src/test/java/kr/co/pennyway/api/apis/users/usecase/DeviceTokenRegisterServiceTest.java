package kr.co.pennyway.api.apis.users.usecase;

import kr.co.pennyway.api.apis.users.service.DeviceTokenRegisterService;
import kr.co.pennyway.api.config.fixture.DeviceTokenFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.context.account.service.DeviceTokenService;
import kr.co.pennyway.domain.context.account.service.UserService;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeviceTokenRegisterServiceTest {
    @InjectMocks
    private DeviceTokenRegisterService deviceTokenRegisterService;

    @Mock
    private UserService userService;

    @Mock
    private DeviceTokenService deviceTokenService;

    private User requestUser;

    @BeforeEach
    void setUp() {
        requestUser = UserFixture.GENERAL_USER.toUser();
    }

    @Test
    @DisplayName("token 등록 요청이 들어왔을 때, 새로운 디바이스 토큰을 등록한다.")
    void registerNewDevice() {
        // given
        given(userService.readUser(requestUser.getId())).willReturn(Optional.of(requestUser));

        DeviceToken expectedDeviceToken = DeviceTokenFixture.INIT.toDevice(requestUser);
        given(deviceTokenService.readDeviceTokenByUserIdAndToken(requestUser.getId(), expectedDeviceToken.getToken()))
                .willReturn(Optional.empty());
        given(deviceTokenService.createDeviceToken(any(DeviceToken.class)))
                .willReturn(expectedDeviceToken);

        // when
        DeviceToken response = deviceTokenRegisterService.execute(requestUser.getId(), expectedDeviceToken.getDeviceId(), expectedDeviceToken.getDeviceName(), expectedDeviceToken.getToken());

        // then
        verify(deviceTokenService).readDeviceTokenByUserIdAndToken(requestUser.getId(), expectedDeviceToken.getToken());
        verify(deviceTokenService).createDeviceToken(any(DeviceToken.class));

        assertEquals(expectedDeviceToken.getToken(), response.getToken());
        assertEquals(requestUser.getId(), response.getUser().getId());
    }

    @Test
    @DisplayName("token에 대한 활성화 디바이스 토큰이 이미 존재하는 경우 기존 데이터를 반환한다.")
    void registerNewDeviceWhenDeviceIsAlreadyExists() {
        // given
        given(userService.readUser(requestUser.getId())).willReturn(Optional.of(requestUser));

        DeviceToken originDeviceToken = DeviceTokenFixture.INIT.toDevice(requestUser);
        given(deviceTokenService.readDeviceTokenByUserIdAndToken(requestUser.getId(), originDeviceToken.getToken()))
                .willReturn(Optional.of(originDeviceToken));

        // when
        DeviceToken result = deviceTokenRegisterService.execute(requestUser.getId(), originDeviceToken.getDeviceId(), originDeviceToken.getDeviceName(), originDeviceToken.getToken());

        // then
        verify(deviceTokenService).readDeviceTokenByUserIdAndToken(requestUser.getId(), originDeviceToken.getToken());
        verify(deviceTokenService, never()).createDeviceToken(any(DeviceToken.class));

        assertTrue(result.getActivated());
        assertNotNull(result.getLastSignedInAt());
    }

    @Test
    @DisplayName("token 등록 요청이 들어왔을 때, 활성화되지 않은 디바이스 토큰이 존재하는 경우 토큰을 활성화 상태로 변경한다.")
    void activateInactiveDevice() {
        /// given
        given(userService.readUser(requestUser.getId())).willReturn(Optional.of(requestUser));

        DeviceToken inactiveToken = DeviceTokenFixture.INIT.toDevice(requestUser);
        inactiveToken.deactivate();
        given(deviceTokenService.readDeviceTokenByUserIdAndToken(requestUser.getId(), inactiveToken.getToken()))
                .willReturn(Optional.of(inactiveToken));

        // when
        DeviceToken result = deviceTokenRegisterService.execute(requestUser.getId(), inactiveToken.getDeviceId(), inactiveToken.getDeviceName(), inactiveToken.getToken());

        // then
        verify(deviceTokenService).readDeviceTokenByUserIdAndToken(requestUser.getId(), inactiveToken.getToken());
        verify(deviceTokenService, never()).createDeviceToken(any(DeviceToken.class));

        assertTrue(result.getActivated());
        assertNotNull(result.getLastSignedInAt());
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 경우 예외를 발생시킨다")
    void throwExceptionForNonExistentUser() {
        // given
        given(userService.readUser(999L)).willReturn(Optional.empty());

        // when & then
        UserErrorException exception = assertThrows(UserErrorException.class,
                () -> deviceTokenRegisterService.execute(999L, "deviceId", "deviceName", "token"));

        assertEquals(UserErrorCode.NOT_FOUND, exception.getBaseErrorCode());
        verify(userService).readUser(999L);
        verifyNoInteractions(deviceTokenService);
    }
}
