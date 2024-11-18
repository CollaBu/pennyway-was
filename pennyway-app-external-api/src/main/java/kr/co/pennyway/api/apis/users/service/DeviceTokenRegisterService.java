package kr.co.pennyway.api.apis.users.service;

import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.service.DeviceTokenService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenRegisterService {
    private final UserService userService;
    private final DeviceTokenService deviceTokenService;

    @Transactional
    public DeviceToken execute(Long userId, String deviceId, String deviceName, String token) {
        User user = userService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));

        return getOrCreateDevice(user, deviceId, deviceName, token);
    }

    /**
     * 사용자의 디바이스 토큰을 가져오거나 생성한다.
     * <p>
     * 이미 등록된 디바이스 토큰인 경우 마지막 로그인 시간을 갱신한다.
     */
    private DeviceToken getOrCreateDevice(User user, String deviceId, String deviceName, String token) {
        Optional<DeviceToken> deviceToken = deviceTokenService.readDeviceByUserIdAndToken(user.getId(), token);

        if (deviceToken.isPresent()) {
            DeviceToken device = deviceToken.get();
            device.activate();
            device.updateLastSignedInAt();
            return device;
        } else {
            return deviceTokenService.createDevice(DeviceToken.of(token, deviceId, deviceName, user));
        }
    }
}
