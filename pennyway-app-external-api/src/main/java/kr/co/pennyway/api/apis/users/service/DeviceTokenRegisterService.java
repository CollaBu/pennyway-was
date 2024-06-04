package kr.co.pennyway.api.apis.users.service;

import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorCode;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorException;
import kr.co.pennyway.domain.domains.device.service.DeviceTokenService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTokenRegisterService {
    private final UserService userService;
    private final DeviceTokenService deviceTokenService;

    @Transactional
    public DeviceToken execute(Long userId, String token) {
        User user = userService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));
        DeviceToken deviceToken = getOrCreateDevice(user, token);

        if (!deviceToken.isActivated()) {
            throw new DeviceTokenErrorException(DeviceTokenErrorCode.NOT_ACTIVATED_DEVICE);
        }

        return deviceToken;
    }

    private DeviceToken getOrCreateDevice(User user, String token) {
        return deviceTokenService.readDeviceByUserIdAndToken(user.getId(), token)
                .orElseGet(() -> deviceTokenService.createDevice(DeviceToken.of(token, user)));
    }
}
