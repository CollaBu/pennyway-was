package kr.co.pennyway.domain.context.account.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.service.DeviceTokenRdbService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserRdbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class DeviceTokenRegisterService {
    private final UserRdbService userRdbService;
    private final DeviceTokenRdbService deviceTokenRdbService;

    /**
     * 사용자의 디바이스 토큰을 생성하거나 갱신한다.
     *
     * <pre>
     * [비즈니스 규칙]
     * - 같은 {userId, deviceId}에 대해 새로운 토큰이 발급될 수 있지만, 활성화된 토큰은 하나여야 합니다.
     * - {deviceId, token} 조합은 시스템 전체에서 유일해야 합니다.
     * - device token이 이미 등록된 경우, 소유자 정보를 갱신하고 마지막 로그인 시간을 갱신한다.
     * - device token이 등록되지 않은 경우, 새로운 device token을 생성한다.
     * </pre>
     *
     * @param userId      사용자 식별자
     * @param deviceId    디바이스 식별자
     * @param deviceName  디바이스 이름
     * @param deviceToken 디바이스 토큰
     * @return {@link DeviceToken} 사용자의 기기로 등록된 Device 정보
     */
    @Transactional
    public DeviceToken execute(Long userId, String deviceId, String deviceName, String deviceToken) {
        User user = userRdbService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));

        return getOrCreateDevice(user, deviceId, deviceName, deviceToken);
    }

    /**
     * 사용자의 디바이스 토큰을 생성합니다.
     * 만약, 이미 등록된 디바이스 토큰이 존재한다면, 해당 토큰을 갱신하고 반환합니다.
     */
    private DeviceToken getOrCreateDevice(User user, String deviceId, String deviceName, String deviceToken) {
        Optional<DeviceToken> device = deviceTokenRdbService.readByDeviceIdAndToken(deviceId, deviceToken);

        if (device.isPresent()) {
            DeviceToken deviceTokenEntity = device.get();
            deviceTokenEntity.handleOwner(user);
            return deviceTokenEntity;
        } else {
            deactivateExistingTokens(user.getId(), deviceId);

            DeviceToken newDeviceToken = DeviceToken.of(deviceToken, deviceId, deviceName, user);

            return deviceTokenRdbService.createDevice(newDeviceToken);
        }
    }

    /**
     * 특정 사용자의 디바이스에 대한 기존 활성 토큰들을 비활성화합니다.
     * 새로운 토큰 등록 시 호출되어 하나의 디바이스에 하나의 활성 토큰만 존재하도록 보장합니다.
     */
    private void deactivateExistingTokens(Long userId, String deviceId) {
        List<DeviceToken> userDeviceTokens = deviceTokenRdbService.readByUserIdAndDeviceId(userId, deviceId);

        userDeviceTokens.stream()
                .filter(DeviceToken::isActivated)
                .forEach(DeviceToken::deactivate);
    }
}
