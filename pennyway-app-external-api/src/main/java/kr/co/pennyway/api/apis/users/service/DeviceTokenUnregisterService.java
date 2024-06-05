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
public class DeviceTokenUnregisterService {
    private final UserService userService;
    private final DeviceTokenService deviceTokenService;

    @Transactional
    public void execute(Long userId, String token) {
        User user = userService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));

        DeviceToken deviceToken = deviceTokenService.readDeviceByUserIdAndToken(user.getId(), token).orElseThrow(
                () -> new DeviceTokenErrorException(DeviceTokenErrorCode.NOT_FOUND_DEVICE)
        );

        deviceTokenService.deleteDevice(deviceToken);
    }
}
