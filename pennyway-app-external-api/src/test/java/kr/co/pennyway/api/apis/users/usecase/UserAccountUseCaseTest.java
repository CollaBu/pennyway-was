package kr.co.pennyway.api.apis.users.usecase;

import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.api.apis.users.helper.PasswordEncoderHelper;
import kr.co.pennyway.api.apis.users.service.DeviceRegisterService;
import kr.co.pennyway.api.apis.users.service.UserProfileUpdateService;
import kr.co.pennyway.api.config.ExternalApiDBTestConfig;
import kr.co.pennyway.api.config.fixture.DeviceFixture;
import kr.co.pennyway.api.config.fixture.UserFixture;
import kr.co.pennyway.domain.config.JpaConfig;
import kr.co.pennyway.domain.domains.device.domain.Device;
import kr.co.pennyway.domain.domains.device.exception.DeviceErrorCode;
import kr.co.pennyway.domain.domains.device.exception.DeviceErrorException;
import kr.co.pennyway.domain.domains.device.service.DeviceService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import kr.co.pennyway.domain.domains.user.type.Role;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.util.AssertionErrors.*;

@ExtendWith(MockitoExtension.class)
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create")
@ContextConfiguration(classes = {JpaConfig.class, UserAccountUseCase.class, DeviceRegisterService.class, UserService.class, DeviceService.class, UserProfileUpdateService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class UserAccountUseCaseTest extends ExternalApiDBTestConfig {
    @Autowired
    private UserService userService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private UserAccountUseCase userAccountUseCase;

    @MockBean
    private PasswordEncoderHelper passwordEncoderHelper;

    @Order(1)
    @Nested
    @DisplayName("[1] 디바이스 등록 테스트")
    class DeviceRegisterTest {
        private User requestUser;

        @BeforeEach
        void setUp() {
            User user = User.builder().role(Role.USER).profileVisibility(ProfileVisibility.PUBLIC).build();
            requestUser = userService.createUser(user);
        }

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
            originDevice.deactivate();
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
        private User requestUser;

        @BeforeEach
        void setUp() {
            User user = User.builder().role(Role.USER).profileVisibility(ProfileVisibility.PUBLIC).build();
            requestUser = userService.createUser(user);
        }

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

    @Order(3)
    @Nested
    @DisplayName("[3] 사용자 이름 수정 테스트")
    class UpdateNameTest {
        @Test
        @Transactional
        @DisplayName("사용자가 삭제된 유저인 경우 NOT_FOUND 에러를 반환한다.")
        void updateNameWhenUserIsDeleted() {
            // given
            String newName = "양재서";
            User originUser = UserFixture.GENERAL_USER.toUser();
            userService.createUser(originUser);
            userService.deleteUser(originUser);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> userAccountUseCase.updateName(originUser.getId(), newName));
            assertEquals("삭제된 사용자인 경우 Not Found를 반환한다.", UserErrorCode.NOT_FOUND, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("사용자의 이름이 성공적으로 변경된다.")
        void updateName() {
            // given
            User originUser = UserFixture.GENERAL_USER.toUser();
            userService.createUser(originUser);
            String newName = "양재서";

            // when
            userAccountUseCase.updateName(originUser.getId(), newName);

            // then
            User updatedUser = userService.readUser(originUser.getId()).orElseThrow();
            assertEquals("사용자 이름이 변경되어 있어야 한다.", newName, updatedUser.getName());
        }
    }

    @Order(4)
    @Nested
    @DisplayName("[4] 사용자 비밀번호 검증 테스트")
    class VerificationPasswordTest {
        private User originUser;

        @BeforeEach
        void setUp() {
            originUser = UserFixture.GENERAL_USER.toUser();
            userService.createUser(originUser);
        }

        @Test
        @Transactional
        @DisplayName("사용자가 삭제된 유저인 경우 NOT_FOUND 에러를 반환한다.")
        void verifyPasswordWhenUserIsDeleted() {
            // given
            userService.deleteUser(originUser);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> userAccountUseCase.verifyPassword(originUser.getId(), originUser.getPassword()));
            assertEquals("삭제된 사용자인 경우 Not Found를 반환한다.", UserErrorCode.NOT_FOUND, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("사용자가 일반 회원가입 이력이 없는 소셜 계정인 경우, DO_NOT_GENERAL_SIGNED_UP 에러를 반환한다.")
        void verifyPasswordWhenUserIsNotGeneralSignedUp() {
            // given
            User originUser = UserFixture.OAUTH_USER.toUser();
            userService.createUser(originUser);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> userAccountUseCase.verifyPassword(originUser.getId(), originUser.getPassword()));
            assertEquals("일반 회원가입 이력이 없는 경우 Do Not General Signed Up을 반환한다.", UserErrorCode.DO_NOT_GENERAL_SIGNED_UP, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("비밀번호가 다른 경우 NOT_MATCHED_PASSWORD 에러를 반환한다.")
        void verifyPasswordWhenPasswordIsNotMatched() {
            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> userAccountUseCase.verifyPassword(originUser.getId(), "notMatchedPassword"));
            assertEquals("비밀번호가 다른 경우 Not Matched Password를 반환한다.", UserErrorCode.NOT_MATCHED_PASSWORD, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("비밀번호가 일치하는 경우 정상적으로 처리된다.")
        void verifyPassword() {
            // given
            given(passwordEncoderHelper.isSamePassword(any(), any())).willReturn(true);

            // when - then
            assertDoesNotThrow(() -> userAccountUseCase.verifyPassword(originUser.getId(), originUser.getPassword()));
        }
    }

    @Order(5)
    @Nested
    @DisplayName("[5] 사용자 비밀번호 변경 테스트")
    class UpdatePasswordTest {
        private User originUser;

        @BeforeEach
        void setUp() {
            originUser = UserFixture.GENERAL_USER.toUser();
            userService.createUser(originUser);
        }

        @Test
        @Transactional
        @DisplayName("사용자가 삭제된 유저인 경우 NOT_FOUND 에러를 반환한다.")
        void updatePasswordWhenUserIsDeleted() {
            // given
            userService.deleteUser(originUser);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> userAccountUseCase.updatePassword(originUser.getId(), originUser.getPassword(), "newPassword"));
            assertEquals("삭제된 사용자인 경우 Not Found를 반환한다.", UserErrorCode.NOT_FOUND, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("oldPassword와 newPassword가 일치하는 경우와 현재 비밀번호와 동일한 비밀번호로 변경을 시도하는 경우, CLIENT_ERROR 에러를 반환한다.")
        void updatePasswordWhenSamePassword() {
            // given
            given(passwordEncoderHelper.isSamePassword(any(), any())).willReturn(true);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> userAccountUseCase.updatePassword(originUser.getId(), originUser.getPassword(), originUser.getPassword()));
            assertEquals("현재 비밀번호와 동일한 비밀번호로 변경할 수 없는 경우 Client Error를 반환한다.", UserErrorCode.PASSWORD_NOT_CHANGED, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("비밀번호가 다른 경우 NOT_MATCHED_PASSWORD 에러를 반환한다.")
        void updatePasswordWhenPasswordIsNotMatched() {
            // given
            given(passwordEncoderHelper.isSamePassword(any(), any())).willReturn(false);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> userAccountUseCase.updatePassword(originUser.getId(), "notMatchedPassword", "newPassword"));
            assertEquals("비밀번호가 다른 경우 Not Matched Password를 반환한다.", UserErrorCode.NOT_MATCHED_PASSWORD, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("사용자가 일반 회원가입 이력이 없는 소셜 계정인 경우, DO_NOT_GENERAL_SIGNED_UP 에러를 반환한다.")
        void updatePasswordWhenUserIsNotGeneralSignedUp() {
            // given
            User originUser = UserFixture.OAUTH_USER.toUser();
            userService.createUser(originUser);

            // when - then
            UserErrorException ex = assertThrows(UserErrorException.class, () -> userAccountUseCase.updatePassword(originUser.getId(), originUser.getPassword(), "newPassword"));
            assertEquals("일반 회원가입 이력이 없는 경우 Do Not General Signed Up을 반환한다.", UserErrorCode.DO_NOT_GENERAL_SIGNED_UP, ex.getBaseErrorCode());
        }

        @Test
        @Transactional
        @DisplayName("정상적인 요청인 경우 비밀번호가 정상적으로 변경된다.")
        void updatePassword() {
            // given
            given(passwordEncoderHelper.isSamePassword(any(), any())).willReturn(true);
            given(passwordEncoderHelper.encodePassword(any())).willReturn("encodedPassword");

            // when - then
            assertDoesNotThrow(() -> userAccountUseCase.updatePassword(originUser.getId(), originUser.getPassword(), "newPassword"));
            assertEquals("비밀번호가 정상적으로 변경되어 있어야 한다.", "encodedPassword", userService.readUser(originUser.getId()).orElseThrow().getPassword());
        }
    }
}