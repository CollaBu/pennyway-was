package kr.co.pennyway.api.apis.users.usecase;

import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.domain.config.JpaConfig;
import kr.co.pennyway.domain.domains.device.domain.Device;
import kr.co.pennyway.domain.domains.device.exception.DeviceErrorCode;
import kr.co.pennyway.domain.domains.device.exception.DeviceErrorException;
import kr.co.pennyway.domain.domains.device.service.DeviceService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import kr.co.pennyway.domain.domains.user.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.*;

@ExtendWith(MockitoExtension.class)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@ContextConfiguration(classes = {JpaConfig.class, UserAccountUseCase.class, UserService.class, DeviceService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserAccountUseCaseTest extends ExternalApiDBTestConfig {
    @Autowired
    private UserService userService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private UserAccountUseCase userAccountUseCase;

    private User requestUser;

    @BeforeEach
    void setUp() {
        User user = User.builder().role(Role.USER).profileVisibility(ProfileVisibility.PUBLIC).build();
        requestUser = userService.createUser(user);
    }

    @Test
    @Transactional
    @DisplayName("[1] 기존에 등록된 디바이스 토큰이 없는 경우, 신규 디바이스를 등록한다.")
    void registerNewDevice() {
        // given
        DeviceDto.RegisterReq request = new DeviceDto.RegisterReq("newToken", "newToken", "modelA", "Windows");

        // when
        DeviceDto.RegisterRes response = userAccountUseCase.registerDevice(requestUser.getId(), request);

        // then
        deviceService.readDeviceByUserIdAndToken(requestUser.getId(), request.newToken()).ifPresentOrElse(
                device -> {
                    assertEquals("요청한 디바이스 토큰과 동일해야 한다.", response.token(), device.getToken());
                    assertEquals("요청한 디바이스 모델과 동일해야 한다.", request.model(), device.getModel());
                    assertEquals("요청한 디바이스 OS와 동일해야 한다.", request.os(), device.getOs());
                    assertEquals("디바이스 ID가 일치해야 한다.", response.id(), device.getId());
                    assertTrue("디바이스가 사용자 ID와 연결되어 있어야 한다.", device.getUser().getId().equals(requestUser.getId()));
                    System.out.println("device = " + device);
                },
                () -> fail("신규 디바이스가 등록되어 있어야 한다.")
        );
    }

    @Test
    @Transactional
    @DisplayName("[2] 기존에 등록된 디바이스 토큰이 있는 경우, 디바이스 토큰을 갱신한다.")
    void updateDeviceToken() {
        // given
        Device oldDevice = Device.of("originToken", "modelA", "Windows", requestUser);
        deviceService.createDevice(oldDevice);
        System.out.println("oldDevice = " + oldDevice);
        DeviceDto.RegisterReq request = new DeviceDto.RegisterReq("originToken", "newToken", "modelA", "Windows");

        // when
        DeviceDto.RegisterRes response = userAccountUseCase.registerDevice(requestUser.getId(), request);

        // then
        deviceService.readDeviceByUserIdAndToken(requestUser.getId(), request.newToken()).ifPresentOrElse(
                device -> {
                    assertEquals("요청한 디바이스 토큰과 동일해야 한다.", response.token(), device.getToken());
                    assertEquals("요청한 디바이스 모델과 동일해야 한다.", request.model(), device.getModel());
                    assertEquals("요청한 디바이스 OS와 동일해야 한다.", request.os(), device.getOs());
                    assertEquals("디바이스 ID가 일치해야 한다.", response.id(), device.getId());
                    assertTrue("디바이스가 사용자 ID와 연결되어 있어야 한다.", device.getUser().getId().equals(requestUser.getId()));
                    System.out.println("device = " + device);
                },
                () -> fail("디바이스 토큰이 갱신되어 있어야 한다.")
        );
    }

    @Test
    @Transactional
    @DisplayName("[2-1] 사용자가 유효한 토큰을 가지고 있지만 모델명이나 OS가 다른 경우 DEVICE_NOT_MATCH 에러를 반환한다.")
    void notMatchDevice() {
        // given
        Device oldDevice = Device.of("originToken", "modelA", "Windows", requestUser);
        deviceService.createDevice(oldDevice);
        System.out.println("oldDevice = " + oldDevice);
        DeviceDto.RegisterReq request = new DeviceDto.RegisterReq("originToken", "newToken", "modelB", "MacOS");

        // when
        DeviceErrorException ex = assertThrows(DeviceErrorException.class, () -> userAccountUseCase.registerDevice(requestUser.getId(), request));
        assertEquals("디바이스 모델명이나 OS가 일치하지 않으면 400 Bad Request를 반환한다.", DeviceErrorCode.NOT_MATCH_DEVICE, ex.getBaseErrorCode());
    }

    @Test
    @Transactional
    @DisplayName("[3] 서버에서 토큰을 비활성화 처리하여 존재하지 않는데, 클라이언트가 변경 요청을 보낸 경우 newToken으로 신규 디바이스를 등록한다.")
    void registerNewDeviceWhenOldDeviceTokenIsNotExists() {
        // given
        DeviceDto.RegisterReq request = new DeviceDto.RegisterReq("originToken", "newToken", "modelA", "Windows");

        // when
        DeviceDto.RegisterRes response = userAccountUseCase.registerDevice(requestUser.getId(), request);

        // then
        deviceService.readDeviceByUserIdAndToken(requestUser.getId(), request.newToken()).ifPresentOrElse(
                device -> {
                    assertEquals("요청한 디바이스 토큰과 동일해야 한다.", response.token(), device.getToken());
                    assertEquals("요청한 디바이스 모델과 동일해야 한다.", request.model(), device.getModel());
                    assertEquals("요청한 디바이스 OS와 동일해야 한다.", request.os(), device.getOs());
                    assertEquals("디바이스 ID가 일치해야 한다.", response.id(), device.getId());
                    assertTrue("디바이스가 사용자 ID와 연결되어 있어야 한다.", device.getUser().getId().equals(requestUser.getId()));
                    System.out.println("device = " + device);
                },
                () -> fail("신규 디바이스가 등록되어 있어야 한다.")
        );
    }
}