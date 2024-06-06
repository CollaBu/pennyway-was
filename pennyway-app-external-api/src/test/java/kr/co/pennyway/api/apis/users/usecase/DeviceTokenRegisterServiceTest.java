package kr.co.pennyway.api.apis.users.usecase;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.pennyway.api.apis.users.dto.DeviceTokenDto;
import kr.co.pennyway.api.apis.users.service.DeviceTokenRegisterService;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.*;

@ExtendWith(MockitoExtension.class)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@ContextConfiguration(classes = {JpaConfig.class, DeviceTokenRegisterService.class, UserService.class, DeviceTokenService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DeviceTokenRegisterServiceTest extends ExternalApiDBTestConfig {
    @Autowired
    private UserService userService;

    @Autowired
    private DeviceTokenService deviceTokenService;

    @Autowired
    private DeviceTokenRegisterService deviceTokenRegisterService;

    @MockBean
    private JPAQueryFactory queryFactory;

    private User requestUser;

    @BeforeEach
    void setUp() {
        requestUser = userService.createUser(UserFixture.GENERAL_USER.toUser());
    }

    @Test
    @Transactional
    @DisplayName("[1] token 등록 요청이 들어왔을 때, 새로운 디바이스 토큰을 등록한다.")
    void registerNewDevice() {
        // given
        DeviceTokenDto.RegisterReq request = DeviceTokenFixture.INIT.toRegisterReq();

        // when
        DeviceToken response = deviceTokenRegisterService.execute(requestUser.getId(), request.token());

        // then
        deviceTokenService.readDeviceByUserIdAndToken(requestUser.getId(), request.token()).ifPresentOrElse(
                device -> {
                    assertEquals("요청한 디바이스 토큰과 동일해야 한다.", response.getToken(), device.getToken());
                    assertEquals("디바이스 ID가 일치해야 한다.", response.getId(), device.getId());
                    assertTrue("디바이스가 사용자 ID와 연결되어 있어야 한다.", device.getUser().getId().equals(requestUser.getId()));
                    System.out.println("device = " + device);
                },
                () -> fail("신규 디바이스가 등록되어 있어야 한다.")
        );
    }

    @Test
    @Transactional
    @DisplayName("[2] token에 대한 활성화 디바이스 토큰이 이미 존재하는 경우 기존 데이터를 반환한다.")
    void registerNewDeviceWhenDeviceIsAlreadyExists() {
        // given
        DeviceToken originDeviceToken = DeviceTokenFixture.INIT.toDevice(requestUser);
        deviceTokenService.createDevice(originDeviceToken);
        DeviceTokenDto.RegisterReq request = DeviceTokenFixture.INIT.toRegisterReq();

        // when
        DeviceToken response = deviceTokenRegisterService.execute(requestUser.getId(), request.token());

        // then
        deviceTokenService.readDeviceByUserIdAndToken(requestUser.getId(), request.token()).ifPresentOrElse(
                device -> {
                    assertEquals("요청한 디바이스 토큰과 동일해야 한다.", response.getToken(), device.getToken());
                    assertEquals("디바이스 ID가 일치해야 한다.", originDeviceToken.getId(), device.getId());
                    assertTrue("디바이스가 사용자 ID와 연결되어 있어야 한다.", device.getUser().getId().equals(requestUser.getId()));
                    assertTrue("디바이스가 활성화 상태여야 한다.", device.getActivated());
                    System.out.println("device = " + device);
                },
                () -> fail("신규 디바이스가 등록되어 있어야 한다.")
        );
    }

    @Test
    @Transactional
    @DisplayName("[3] token 등록 요청이 들어왔을 때, 활성화되지 않은 디바이스 토큰이 존재하는 경우 NOT_ACTIVATED_DEVICE 에러를 반환한다.")
    void registerNewDeviceWhenDeviceIsNotActivated() {
        // given
        DeviceToken originDeviceToken = DeviceTokenFixture.INIT.toDevice(requestUser);
        originDeviceToken.deactivate();
        deviceTokenService.createDevice(originDeviceToken);
        DeviceTokenDto.RegisterReq request = DeviceTokenFixture.INIT.toRegisterReq();

        // when - then
        DeviceTokenErrorException ex = assertThrows(DeviceTokenErrorException.class, () -> deviceTokenRegisterService.execute(requestUser.getId(), request.token()));
        assertEquals("활성화되지 않은 디바이스 토큰이 존재하는 경우 Not Activated Device를 반환한다.", DeviceTokenErrorCode.NOT_ACTIVATED_DEVICE, ex.getBaseErrorCode());
    }
}
