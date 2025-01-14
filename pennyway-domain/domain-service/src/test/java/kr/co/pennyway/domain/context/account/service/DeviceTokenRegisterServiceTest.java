package kr.co.pennyway.domain.context.account.service;

import kr.co.pennyway.domain.context.account.collection.DeviceTokenRegisterCollection;
import kr.co.pennyway.domain.context.common.fixture.UserFixture;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorException;
import kr.co.pennyway.domain.domains.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceTokenRegisterServiceTest {
    @Test
    @DisplayName("새로운 토큰 등록 시 올바른 정보로 생성됩니다")
    void when_user_has_no_token_should_create_new_token() {
        // given
        DeviceTokenRegisterCollection deviceTokenRegisterCollection = new DeviceTokenRegisterCollection();

        User user = UserFixture.GENERAL_USER.toUserWithCustomSetting(1L, "jayang", "Yang", UserFixture.GENERAL_USER.getNotifySetting());
        String expectedToken = "token1";
        String expectedDeviceId = "재서의 까리한 플립";
        String expectedDeviceName = "Galaxy Flip 6";

        // when
        DeviceToken actual = deviceTokenRegisterCollection.register(user, expectedDeviceId, expectedDeviceName, expectedToken);

        // then
        assertEquals(expectedToken, actual.getToken());
        assertEquals(expectedDeviceId, actual.getDeviceId());
        assertEquals(expectedDeviceName, actual.getDeviceName());
        assertEquals(user, actual.getUser());
    }

    @Test
    @DisplayName("이미 소유 중인 토큰인 경우 마지막 로그인 날짜만 갱신합니다")
    void when_token_exists_should_update_last_signed_at() {
        // given
        User owner = UserFixture.GENERAL_USER.toUser();
        String token = "token1";
        String deviceId = "device1";
        DeviceToken existingToken = DeviceToken.of(token, deviceId, "Android", owner);
        DeviceTokenRegisterCollection collection = new DeviceTokenRegisterCollection(existingToken);

        // when
        DeviceToken actual = collection.register(owner, deviceId, "Android", token);

        // then
        assertEquals(existingToken, actual);
        assertEquals(owner, actual.getUser());
        assertTrue(actual.isActivated());
    }

    @Test
    @DisplayName("동일한 deviceToken이 이미 비활성화된 상태로 등록되어 있다면, 소유자와 마지막 로그인 시간을 변경하고 토큰을 활성화한다.")
    void when_token_exists_should_update_owner_and_last_signed_at() {
        // given
        User originalOwner = UserFixture.GENERAL_USER.toUserWithCustomSetting(1L, "jayang", "Yang", UserFixture.GENERAL_USER.getNotifySetting());
        DeviceToken existingToken = DeviceToken.of("token1", "device1", "Android", originalOwner);
        existingToken.deactivate();
        DeviceTokenRegisterCollection deviceTokenRegisterCollection = new DeviceTokenRegisterCollection(existingToken);

        User newOwner = UserFixture.GENERAL_USER.toUserWithCustomSetting(2L, "another", "User", UserFixture.GENERAL_USER.getNotifySetting());
        String expectedToken = "token1";
        String expectedDeviceId = "device1";
        String expectedDeviceName = "Android";

        // when
        DeviceToken actual = deviceTokenRegisterCollection.register(newOwner, expectedDeviceId, expectedDeviceName, expectedToken);

        // then
        assertTrue(actual.isActivated());
        assertEquals(expectedToken, actual.getToken());
        assertEquals(expectedDeviceId, actual.getDeviceId());
        assertEquals(expectedDeviceName, actual.getDeviceName());
        assertEquals(newOwner, actual.getUser());
    }

    @Test
    @DisplayName("새로운 토큰 등록 시, 디바이스의 기존 활성 토큰들은 비활성화되고, 새로 등록된 토큰이 반환됩니다")
    void when_registering_new_token_should_deactivate_existing_token_for_same_device() {
        // given
        User user = UserFixture.GENERAL_USER.toUser();
        DeviceToken existingToken = DeviceToken.of("oldToken", "device1", "Android", user);
        List<DeviceToken> userTokens = List.of(existingToken);

        String expectedToken = "newToken";

        DeviceTokenRegisterCollection collection = new DeviceTokenRegisterCollection(null, userTokens);

        // when
        DeviceToken actual = collection.register(user, "device1", "Android", expectedToken);

        // then
        assertFalse(existingToken.isActivated());
        assertTrue(actual.isActivated());
        assertEquals(expectedToken, actual.getToken());
    }


    @Test
    @DisplayName("다른 디바이스에서 이미 사용 중인 활성화 토큰으로 등록을 시도하면 예외가 발생합니다")
    void should_throw_duplicate_exception_when_token_exists_for_different_device_id() {
        // given
        User owner = UserFixture.GENERAL_USER.toUserWithCustomSetting(1L, "jayang", "Yang", UserFixture.GENERAL_USER.getNotifySetting());
        User hacker = UserFixture.GENERAL_USER.toUserWithCustomSetting(2L, "another", "User", UserFixture.GENERAL_USER.getNotifySetting());

        String token = "token1";
        DeviceToken existingToken = DeviceToken.of(token, "token1", "Android", owner);

        DeviceTokenRegisterCollection deviceTokenRegisterCollection = new DeviceTokenRegisterCollection(existingToken);

        // when & then
        assertThrows(DeviceTokenErrorException.class, () -> deviceTokenRegisterCollection.register(hacker, "HACKER_DEVICE_ID", "Android", token));
    }

    @Test
    @DisplayName("비활성화된 토큰은 다른 디바이스에서도 재사용할 수 있습니다")
    void when_token_deactivated_should_allow_reuse_on_different_device() {
        // given
        User user = UserFixture.GENERAL_USER.toUser();
        String token = "token1";
        String newDeviceId = "newDeviceId";

        DeviceToken existingToken = DeviceToken.of(token, "old-device", "Android", user);
        existingToken.deactivate();
        DeviceTokenRegisterCollection collection = new DeviceTokenRegisterCollection(existingToken);

        // when
        DeviceToken actual = collection.register(user, newDeviceId, "Android", token);

        // then
        assertEquals(existingToken, actual);
        assertEquals(newDeviceId, actual.getDeviceId());
        assertTrue(actual.isActivated());
    }

    @Test
    @DisplayName("같은 사용자가 같은 디바이스에 대해 새로운 토큰을 등록하면 기존 토큰은 비활성화됩니다")
    void when_registering_different_token_for_same_device_should_deactivate_existing() {
        // given
        User user = UserFixture.GENERAL_USER.toUser();
        DeviceToken existingToken = DeviceToken.of("oldToken", "device1", "Android", user);
        List<DeviceToken> userTokens = List.of(existingToken);

        DeviceTokenRegisterCollection collection = new DeviceTokenRegisterCollection(null, userTokens);

        // when
        DeviceToken result = collection.register(user, "device1", "Android", "newToken");

        // then
        assertFalse(existingToken.isActivated());
        assertTrue(result.isActivated());
        assertEquals("newToken", result.getToken());
    }
}
