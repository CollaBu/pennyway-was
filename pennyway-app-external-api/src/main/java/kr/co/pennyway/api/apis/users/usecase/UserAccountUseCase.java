package kr.co.pennyway.api.apis.users.usecase;

import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.device.domain.Device;
import kr.co.pennyway.domain.domains.device.service.DeviceService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UserAccountUseCase {
    private final UserService userService;
    private final DeviceService deviceService;

    @Transactional
    public Long registerDevice(Long userId, DeviceDto.RegisterReq request) {
        User user = userService.readUser(userId).orElseThrow(
                () -> new UserErrorException(UserErrorCode.NOT_FOUND)
        );


        if (request.isSameToken()) {
            Device device = deviceService.createDevice(request.toEntity(user));
            return device.getId();
        }

        boolean flag = deviceService.isExistDeviceByToken(request.originToken()); // true: newToken으로 업데이트, false: 새로 저장

        if (flag) { // newToken으로 업데이트
            Device device = deviceService.readDevicesByUserId(userId).stream()
                    .filter(d -> d.getToken().equals(request.originToken()))
                    .findFirst()
                    .orElseThrow();
            device.updateToken(request.newToken());
            return device.getId();
        } else { // newToken으로 새로 저장
            Device device = request.toEntity(user);
            deviceService.createDevice(device);
            return device.getId();
        }
    }


}
