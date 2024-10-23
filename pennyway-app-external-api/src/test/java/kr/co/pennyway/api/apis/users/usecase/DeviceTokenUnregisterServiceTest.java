package kr.co.pennyway.api.apis.users.usecase;

import kr.co.pennyway.api.apis.users.service.DeviceTokenUnregisterService;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.TestJpaConfig;
import kr.co.pennyway.api.config.fixture.DeviceTokenFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.config.JpaConfig;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorCode;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorException;
import kr.co.pennyway.domain.domains.device.service.DeviceTokenService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertFalse;

@ExtendWith(MockitoExtension.class)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@ContextConfiguration(classes = {JpaConfig.class, DeviceTokenUnregisterService.class, UserService.class, DeviceTokenService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfig.class)
public class DeviceTokenUnregisterServiceTest extends ExternalApiDBTestConfig {
    @Autowired
    private UserService userService;

    @Autowired
    private DeviceTokenService deviceTokenService;

    @Autowired
    private DeviceTokenUnregisterService deviceTokenUnregisterService;

    private User requestUser;

    @BeforeEach
    void setUp() {
        requestUser = userService.createUser(UserFixture.GENERAL_USER.toUser());
    }

    @Test
    @Transactional
    @DisplayName("사용자 ID와 origin token에 매칭되는 활성 디바이스가 존재하는 경우 디바이스를 비활성화한다.")
    void unregisterDevice() {
        // given
        DeviceToken deviceToken = DeviceTokenFixture.INIT.toDevice(requestUser);
        deviceTokenService.createDevice(deviceToken);

        // when
        deviceTokenUnregisterService.execute(requestUser.getId(), deviceToken.getToken());

        // then
        DeviceToken deletedDevice = deviceTokenService.readDeviceByUserIdAndToken(requestUser.getId(), deviceToken.getToken()).get();
        assertFalse("디바이스가 비활성화 되어있어야 한다.", deletedDevice.isActivated());
    }

    @Test
    @Transactional
    @DisplayName("사용자 ID와 token에 매칭되는 디바이스가 존재하지 않는 경우 NOT_FOUND_DEVICE 에러를 반환한다.")
    void unregisterDeviceWhenDeviceIsNotExists() {
        // given
        DeviceToken deviceToken = DeviceTokenFixture.INIT.toDevice(requestUser);
        deviceTokenService.createDevice(deviceToken);

        // when - then
        DeviceTokenErrorException ex = assertThrows(DeviceTokenErrorException.class, () -> deviceTokenUnregisterService.execute(requestUser.getId(), "notExistsToken"));
        assertEquals("디바이스 토큰이 존재하지 않으면 Not Found를 반환한다.", DeviceTokenErrorCode.NOT_FOUND_DEVICE, ex.getBaseErrorCode());
    }
}
