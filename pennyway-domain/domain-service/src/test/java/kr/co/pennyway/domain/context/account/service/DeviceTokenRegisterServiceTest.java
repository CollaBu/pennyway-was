package kr.co.pennyway.domain.context.account.service;

import kr.co.pennyway.domain.context.account.collection.DeviceTokenCollection;
import kr.co.pennyway.domain.context.common.fixture.UserFixture;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorException;
import kr.co.pennyway.domain.domains.device.service.DeviceTokenRdbService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserRdbService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DeviceTokenRegisterServiceTest {
    @Mock
    private UserRdbService userRdbService;

    @Mock
    private DeviceTokenRdbService deviceTokenRdbService;

    @InjectMocks
    private DeviceTokenRegisterService deviceTokenRegisterService;

    @Test
    @DisplayName("이미 소유 중인 토큰인 경우 마지막 로그인 날짜만 갱신합니다")
    void shouldUpdateOwnerWhenTokenExists() {
        // given
        User user = UserFixture.GENERAL_USER.toUser();
        String expectedToken = "token1";
        String expectedDeviceId = "device1";
        String expectedDeviceName = "Android";
        DeviceToken existingToken = DeviceToken.of(expectedToken, expectedDeviceId, expectedDeviceName, user);

        given(userRdbService.readUser(1L)).willReturn(Optional.of(user));
        given(deviceTokenRdbService.readDeviceByToken(expectedToken)).willReturn(Optional.of(existingToken));

        // when
        DeviceToken result = deviceTokenRegisterService.execute(1L, expectedDeviceId, expectedDeviceName, expectedToken);

        // then
        assertEquals(existingToken, result);
        assertEquals(user, result.getUser());
        assertTrue(existingToken.isActivated());

        verify(deviceTokenRdbService, never()).createDevice(any());
    }

    @Test
    @DisplayName("새로운 토큰 등록 시 기존 활성 토큰들은 비활성화되고, 새로 등록된 토큰이 반환됩니다")
    void shouldDeactivateExistingTokensWhenRegisteringNew() {
        // given
        User user = UserFixture.GENERAL_USER.toUserWithCustomSetting(1L, "jayang", "Yang", UserFixture.GENERAL_USER.getNotifySetting());
        String expectedToken = "oldToken";
        String expectedDeviceId = "device1";
        String expectedDeviceName = "Android";
        String expectedNewToken = "newToken";
        DeviceToken originToken = DeviceToken.of(expectedToken, expectedDeviceId, expectedDeviceName, user);

        given(userRdbService.readUser(user.getId())).willReturn(Optional.of(user));
        given(deviceTokenRdbService.readDeviceByToken(expectedNewToken)).willReturn(Optional.empty());
        given(deviceTokenRdbService.readByUserIdAndDeviceId(user.getId(), expectedDeviceId)).willReturn(List.of(originToken));
        given(deviceTokenRdbService.createDevice(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        DeviceToken result = deviceTokenRegisterService.execute(user.getId(), expectedDeviceId, expectedDeviceName, expectedNewToken);

        // then
        assertTrue(originToken.isExpired());
        assertEquals(expectedNewToken, result.getToken());
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면 예외가 발생합니다")
    void shouldThrowExceptionWhenUserNotFound() {
        // given
        given(userRdbService.readUser(1L)).willReturn(Optional.empty());

        // when & then
        assertThrows(UserErrorException.class, () ->
                deviceTokenRegisterService.execute(1L, "device1", "Android", "token1"));
    }

    @Test
    @DisplayName("새로운 토큰 등록 시 올바른 정보로 생성됩니다")
    void when_user_has_no_token_should_create_new_token() {
        // given
        DeviceTokenCollection deviceTokenCollection = new DeviceTokenCollection();

        User user = UserFixture.GENERAL_USER.toUserWithCustomSetting(1L, "jayang", "Yang", UserFixture.GENERAL_USER.getNotifySetting());
        String expectedToken = "token1";
        String expectedDeviceId = "재서의 까리한 플립";
        String expectedDeviceName = "Galaxy Flip 6";

        // when
        DeviceToken actual = deviceTokenCollection.register(user, expectedDeviceId, expectedDeviceName, expectedToken);

        // then
        assertEquals(expectedToken, actual.getToken());
        assertEquals(expectedDeviceId, actual.getDeviceId());
        assertEquals(expectedDeviceName, actual.getDeviceName());
        assertEquals(user, actual.getUser());
    }

    @Test
    @DisplayName("기기의 토큰이 다른 소유자에게 등록되어 있지만 deviceId가 같은 경우, 소유자 정보를 갱신합니다")
    void shouldUpdateOwnerWhenTokenExistsWithDifferentUser() {
        // given
        User user = UserFixture.GENERAL_USER.toUserWithCustomSetting(1L, "jayang", "Yang", UserFixture.GENERAL_USER.getNotifySetting());
        User anotherUser = UserFixture.GENERAL_USER.toUserWithCustomSetting(2L, "another", "User", UserFixture.GENERAL_USER.getNotifySetting());
        String expectedToken = "token1";
        String expectedDeviceId = "device1";
        String expectedDeviceName = "Android";
        DeviceToken existingToken = DeviceToken.of(expectedToken, expectedDeviceId, expectedDeviceName, anotherUser);

        given(userRdbService.readUser(user.getId())).willReturn(Optional.of(user));
        given(deviceTokenRdbService.readDeviceByToken(expectedToken)).willReturn(Optional.of(existingToken));

        // when
        DeviceToken result = deviceTokenRegisterService.execute(user.getId(), expectedDeviceId, expectedDeviceName, expectedToken);

        // then
        assertEquals(existingToken, result);
        assertEquals(user, result.getUser());
        assertTrue(existingToken.isActivated());

        verify(deviceTokenRdbService, never()).createDevice(any());
    }

    @Test
    @DisplayName("다른 디바이스에서 이미 사용 중인 토큰으로 등록을 시도하면 예외가 발생합니다")
    void shouldThrowExceptionWhenTokenExistsForDifferentDevice() {
        // given
        User user = UserFixture.GENERAL_USER.toUser();
        String token = "token1";
        String deviceId = "device2";
        DeviceToken existingToken = DeviceToken.of(token, "device1", "Android", user);

        given(userRdbService.readUser(user.getId())).willReturn(Optional.of(user));
        given(deviceTokenRdbService.readDeviceByToken(token)).willReturn(Optional.of(existingToken));

        // when & then
        assertThrows(DeviceTokenErrorException.class, () ->
                deviceTokenRegisterService.execute(user.getId(), deviceId, "Android", token));
    }

    @Test
    @DisplayName("다른 디바이스에 이미 등록된 토큰이지만 비활성화된 경우 소유권을 갱신하고 활성화합니다")
    void shouldUpdateOwnerAndActivateWhenTokenExistsButDeactivated() {
        // given
        User user = UserFixture.GENERAL_USER.toUser();
        String token = "token1";
        String oldDeviceId = "device1";
        String newDeviceId = "device2";
        DeviceToken existingToken = DeviceToken.of(token, oldDeviceId, "Android", user);
        existingToken.deactivate();

        given(userRdbService.readUser(user.getId())).willReturn(Optional.of(user));
        given(deviceTokenRdbService.readDeviceByToken(token)).willReturn(Optional.of(existingToken));

        // when
        DeviceToken result = deviceTokenRegisterService.execute(user.getId(), newDeviceId, "Android", token);

        // then
        assertEquals(existingToken, result);
        assertEquals(user, result.getUser());
        assertTrue(result.isActivated());
        assertEquals(newDeviceId, result.getDeviceId());

        verify(deviceTokenRdbService, never()).createDevice(any());
    }

    @Test
    @DisplayName("같은 사용자가 여러 기기에 토큰을 등록할 수 있습니다")
    void shouldAllowSameUserToRegisterMultipleDevices() {
        // given
        User user = UserFixture.GENERAL_USER.toUser();
        String device1Token = "token1";
        String device2Token = "token2";

        given(userRdbService.readUser(user.getId())).willReturn(Optional.of(user));
        given(deviceTokenRdbService.readDeviceByToken(device1Token)).willReturn(Optional.empty());
        given(deviceTokenRdbService.readDeviceByToken(device2Token)).willReturn(Optional.empty());
        given(deviceTokenRdbService.createDevice(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        DeviceToken result1 = deviceTokenRegisterService.execute(user.getId(), "device1", "Android", device1Token);
        DeviceToken result2 = deviceTokenRegisterService.execute(user.getId(), "device2", "iPhone", device2Token);

        // then
        assertTrue(result1.isActivated());
        assertTrue(result2.isActivated());
        assertNotEquals(result1.getDeviceId(), result2.getDeviceId());
    }

    @Test
    @DisplayName("이미 등록된 토큰에 대해 같은 디바이스로 재등록을 시도하면 소유자 정보만 갱신됩니다")
    void shouldUpdateOwnerWhenTokenExistsForSameDevice() {
        // given
        User user = UserFixture.GENERAL_USER.toUser();
        String token = "token1";
        String deviceId = "device1";
        User previousOwner = UserFixture.GENERAL_USER.toUserWithCustomSetting(2L, "other", "User", UserFixture.GENERAL_USER.getNotifySetting());
        DeviceToken existingToken = DeviceToken.of(token, deviceId, "Android", previousOwner);

        given(userRdbService.readUser(user.getId())).willReturn(Optional.of(user));
        given(deviceTokenRdbService.readDeviceByToken(token)).willReturn(Optional.of(existingToken));

        // when
        DeviceToken result = deviceTokenRegisterService.execute(user.getId(), deviceId, "Android", token);

        // then
        assertEquals(existingToken, result);
        assertEquals(user, result.getUser());
        assertTrue(result.isActivated());
        verify(deviceTokenRdbService, never()).createDevice(any());
    }

    @Test
    @DisplayName("같은 사용자가 같은 디바이스에 대해 새로운 토큰을 등록하면 기존 토큰은 비활성화됩니다")
    void shouldDeactivateExistingTokensWhenRegisteringNewTokenForSameDevice() {
        // given
        User user = UserFixture.GENERAL_USER.toUser();
        String oldToken = "oldToken";
        String newToken = "newToken";
        String deviceId = "device1";
        DeviceToken existingToken = DeviceToken.of(oldToken, deviceId, "Android", user);
        existingToken.activate();

        given(userRdbService.readUser(user.getId())).willReturn(Optional.of(user));
        given(deviceTokenRdbService.readDeviceByToken(newToken)).willReturn(Optional.empty());
        given(deviceTokenRdbService.readByUserIdAndDeviceId(user.getId(), deviceId))
                .willReturn(List.of(existingToken));
        given(deviceTokenRdbService.createDevice(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        DeviceToken result = deviceTokenRegisterService.execute(user.getId(), deviceId, "Android", newToken);

        // then
        assertFalse(existingToken.isActivated());
        assertTrue(result.isActivated());
        assertEquals(newToken, result.getToken());
    }
}
