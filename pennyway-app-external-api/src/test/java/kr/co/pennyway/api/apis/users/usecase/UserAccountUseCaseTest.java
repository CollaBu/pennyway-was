package kr.co.pennyway.api.apis.users.usecase;

import jakarta.persistence.EntityManager;
import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.api.apis.users.service.DeviceRegisterService;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.fixture.DeviceFixture;
import kr.co.pennyway.domain.config.JpaConfig;
import kr.co.pennyway.domain.domains.device.domain.Device;
import kr.co.pennyway.domain.domains.device.exception.DeviceErrorCode;
import kr.co.pennyway.domain.domains.device.exception.DeviceErrorException;
import kr.co.pennyway.domain.domains.device.service.DeviceService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import kr.co.pennyway.domain.domains.user.type.Role;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.*;

@ExtendWith(MockitoExtension.class)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@ContextConfiguration(classes = {JpaConfig.class, UserAccountUseCase.class, DeviceRegisterService.class, UserService.class, DeviceService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserAccountUseCaseTest extends ExternalApiDBTestConfig {
    @Autowired
    private UserService userService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private UserAccountUseCase userAccountUseCase;

    @Autowired
    private EntityManager em;

    private User requestUser;

    @BeforeEach
    void setUp() {
        User user = User.builder().role(Role.USER).profileVisibility(ProfileVisibility.PUBLIC).build();
        requestUser = userService.createUser(user);
    }

    @Order(1)
    @Nested
    @DisplayName("[1] 디바이스 등록 테스트")
    class DeviceRegisterTest {
        @Test
        @Transactional
        @DisplayName("[1] originToken과 newToken이 같은 경우, 신규 디바이스를 등록한다.")
        void registerNewDevice() {
            // given
            DeviceDto.RegisterReq request = DeviceFixture.INIT.toRegisterReq();

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
        @DisplayName("[1-1] 저장 요청에서 originToken에 대한 디바이스가 이미 존재하는 경우, 디바이스 정보 변경 사항만 업데이트하고 기존 디바이스 정보를 반환한다.")
        void registerNewDeviceWhenDeviceIsAlreadyExists() {
            // given
            Device originDevice = DeviceFixture.ORIGIN_DEVICE.toDevice(requestUser);
            deviceService.createDevice(originDevice);
            DeviceDto.RegisterReq request = DeviceFixture.ONLY_MODEL_AND_OS_CHANGED.toRegisterReq();

            // when
            DeviceDto.RegisterRes response = userAccountUseCase.registerDevice(requestUser.getId(), request);

            // then
            deviceService.readDeviceByUserIdAndToken(requestUser.getId(), request.newToken()).ifPresentOrElse(
                    device -> {
                        assertEquals("요청한 디바이스 토큰과 동일해야 한다.", response.token(), device.getToken());
                        assertEquals("요청한 디바이스 모델과 동일해야 한다.", request.model(), device.getModel());
                        assertEquals("요청한 디바이스 OS와 동일해야 한다.", request.os(), device.getOs());
                        assertEquals("디바이스 ID가 일치해야 한다.", originDevice.getId(), device.getId());
                        assertTrue("디바이스가 사용자 ID와 연결되어 있어야 한다.", device.getUser().getId().equals(requestUser.getId()));
                        assertTrue("디바이스가 활성화 상태여야 한다.", device.getActivated());
                        System.out.println("device = " + device);
                    },
                    () -> fail("신규 디바이스가 등록되어 있어야 한다.")
            );
        }

        @Test
        @Transactional
        @DisplayName("[2] originToken과 일치하는 활성화 디바이스 토큰이 존재한다면, 디바이스 토큰을 갱신한다.")
        void updateActivateDeviceToken() {
            // given
            Device originDevice = DeviceFixture.ORIGIN_DEVICE.toDevice(requestUser);
            deviceService.createDevice(originDevice);

            System.out.println("originDevice = " + originDevice);
            DeviceDto.RegisterReq request = DeviceFixture.ONLY_TOKEN_CHANGED.toRegisterReq();

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
                        assertTrue("디바이스가 활성화 상태여야 한다.", device.getActivated());
                        System.out.println("device = " + device);
                    },
                    () -> fail("디바이스 토큰이 갱신되어 있어야 한다.")
            );
        }

        @Test
        @Transactional
        @DisplayName("[2-1] 기존에 등록된 비활성화 디바이스 토큰이 있고 디바이스 정보가 일치한다면, 디바이스 토큰을 갱신하고 활성화로 변경한다.")
        void updateDeactivateDeviceToken() {
            // given
            Device originDevice = DeviceFixture.ORIGIN_DEVICE.toDevice(requestUser);
            deviceService.createDevice(originDevice);
            em.createQuery("UPDATE Device d SET d.activated = false WHERE d.id = :id AND d.token = :token")
                    .setParameter("id", originDevice.getId())
                    .setParameter("token", originDevice.getToken())
                    .executeUpdate(); // 비활성화 처리

            System.out.println("originDevice = " + originDevice);
            DeviceDto.RegisterReq request = DeviceFixture.ONLY_TOKEN_CHANGED.toRegisterReq();

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
                        assertTrue("디바이스가 활성화 상태여야 한다.", device.getActivated());
                        System.out.println("device = " + device);
                    },
                    () -> fail("디바이스 토큰이 갱신되어 있어야 한다.")
            );
        }


        @Test
        @Transactional
        @DisplayName("[2-2] 사용자가 유효한 토큰을 가지고 있지만 모델명이나 OS가 다른 경우, 디바이스 정보를 업데이트한다.")
        void notMatchDevice() {
            // given
            Device originDevice = DeviceFixture.ORIGIN_DEVICE.toDevice(requestUser);
            deviceService.createDevice(originDevice);
            System.out.println("originDevice = " + originDevice);
            DeviceDto.RegisterReq request = DeviceFixture.ALL_CHANGED.toRegisterReq();

            // when
            userAccountUseCase.registerDevice(requestUser.getId(), request);

            // then
            deviceService.readDeviceByUserIdAndToken(requestUser.getId(), request.newToken()).ifPresentOrElse(
                    device -> {
                        assertEquals("요청한 디바이스 토큰과 동일해야 한다.", request.newToken(), device.getToken());
                        assertEquals("요청한 디바이스 모델과 동일해야 한다.", request.model(), device.getModel());
                        assertEquals("요청한 디바이스 OS와 동일해야 한다.", request.os(), device.getOs());
                        assertTrue("디바이스가 사용자 ID와 연결되어 있어야 한다.", device.getUser().getId().equals(requestUser.getId()));
                        assertTrue("디바이스가 활성화 상태여야 한다.", device.getActivated());
                        System.out.println("device = " + device);
                    },
                    () -> fail("디바이스 토큰이 갱신되어 있어야 한다.")
            );
        }

        @Test
        @Transactional
        @DisplayName("[3] 토큰 수정 요청에서 oldToken에 대한 디바이스가 존재하지 않는 경우, NOT_FOUND 에러를 반환한다.")
        void registerNewDeviceWhenOldDeviceTokenIsNotExists() {
            // given
            DeviceDto.RegisterReq request = DeviceFixture.ONLY_TOKEN_CHANGED.toRegisterReq();

            // when - then
            DeviceErrorException ex = assertThrows(DeviceErrorException.class, () -> userAccountUseCase.registerDevice(requestUser.getId(), request));
            assertEquals("디바이스 토큰이 존재하지 않으면 Not Found를 반환한다.", DeviceErrorCode.NOT_FOUND_DEVICE, ex.getBaseErrorCode());
        }
    }

    @Order(2)
    @Nested
    @DisplayName("[2] 디바이스 삭제 테스트")
    class DeviceUnregisterTest {
        @Test
        @Transactional
        @DisplayName("사용자 ID와 origin token에 매칭되는 활성 디바이스가 존재하는 경우 디바이스를 삭제한다.")
        void unregisterDevice() {
            // given
            Device device = DeviceFixture.ORIGIN_DEVICE.toDevice(requestUser);
            deviceService.createDevice(device);

            // when
            userAccountUseCase.unregisterDevice(requestUser.getId(), device.getToken());

            // then
            Optional<Device> deletedDevice = deviceService.readDeviceByUserIdAndToken(requestUser.getId(), device.getToken());
            assertNull("디바이스가 삭제되어 있어야 한다.", deletedDevice.orElse(null));
        }

        @Test
        @Transactional
        @DisplayName("사용자 ID와 token에 매칭되는 디바이스가 존재하지 않는 경우 NOT_FOUND_DEVICE 에러를 반환한다.")
        void unregisterDeviceWhenDeviceIsNotExists() {
            // given
            Device device = DeviceFixture.ORIGIN_DEVICE.toDevice(requestUser);
            deviceService.createDevice(device);

            // when - then
            DeviceErrorException ex = assertThrows(DeviceErrorException.class, () -> userAccountUseCase.unregisterDevice(requestUser.getId(), "notExistsToken"));
            assertEquals("디바이스 토큰이 존재하지 않으면 Not Found를 반환한다.", DeviceErrorCode.NOT_FOUND_DEVICE, ex.getBaseErrorCode());
        }
    }


}