package kr.co.pennyway.api.apis.users.service;

import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorCode;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorException;
import kr.co.pennyway.domain.domains.device.service.DeviceTokenService;
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
        DeviceToken deviceToken = deviceTokenService.readDeviceByUserIdAndToken(userId, token).orElseThrow(
                () -> new DeviceTokenErrorException(DeviceTokenErrorCode.NOT_FOUND_DEVICE)
        );

        deviceToken.deactivate();
    }
}
