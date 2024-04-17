package kr.co.pennyway.api.apis.users.usecase;

import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.domain.config.JpaConfig;
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

    private Long requestUserId;

    @BeforeEach
    void setUp() {
        User user = User.builder().role(Role.USER).profileVisibility(ProfileVisibility.PUBLIC).build();
        requestUserId = userService.createUser(user).getId();
    }

    @Test
    @Transactional
    @DisplayName("[1] 기존에 등록된 디바이스 토큰이 없는 경우, 신규 디바이스를 등록한다.")
    void registerNewDevice() {
        // given
        DeviceDto.RegisterReq request = new DeviceDto.RegisterReq("newToken", "newToken", "modelA", "Windows");

        // when
        Long actualDeviceId = userAccountUseCase.registerDevice(requestUserId, request);

        // then
        deviceService.readDeviceByUserIdAndToken(requestUserId, request.newToken()).ifPresentOrElse(
                device -> {
                    assertEquals("요청한 디바이스 토큰과 동일해야 한다.", request.newToken(), device.getToken());
                    assertEquals("요청한 디바이스 모델과 동일해야 한다.", request.model(), device.getModel());
                    assertEquals("요청한 디바이스 OS와 동일해야 한다.", request.os(), device.getOs());
                    assertEquals("디바이스 ID가 일치해야 한다.", actualDeviceId, device.getId());
                    assertTrue("디바이스가 사용자 ID와 연결되어 있어야 한다.", device.getUser().getId().equals(requestUserId));
                },
                () -> fail("신규 디바이스가 등록되어 있어야 한다.")
        );
    }

    @Test
    @Transactional
    @DisplayName("[2] 기존에 등록된 디바이스 토큰이 있는 경우, 디바이스 토큰을 갱신한다.")
    void updateDeviceToken() {
        // given
        DeviceDto.RegisterReq request = new DeviceDto.RegisterReq("newToken", "newToken", "modelA", "Windows");

        // when


        // then

    }


}