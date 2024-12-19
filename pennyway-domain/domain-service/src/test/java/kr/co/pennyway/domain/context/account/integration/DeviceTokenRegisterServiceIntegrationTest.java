package kr.co.pennyway.domain.context.account.integration;

import kr.co.pennyway.domain.common.repository.ExtendedRepositoryFactory;
import kr.co.pennyway.domain.config.DomainServiceIntegrationProfileResolver;
import kr.co.pennyway.domain.config.DomainServiceTestInfraConfig;
import kr.co.pennyway.domain.config.JpaTestConfig;
import kr.co.pennyway.domain.context.account.service.DeviceTokenRegisterService;
import kr.co.pennyway.domain.context.common.fixture.UserFixture;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorCode;
import kr.co.pennyway.domain.domains.device.exception.DeviceTokenErrorException;
import kr.co.pennyway.domain.domains.device.repository.DeviceTokenRepository;
import kr.co.pennyway.domain.domains.device.service.DeviceTokenRdbService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.repository.UserRepository;
import kr.co.pennyway.domain.domains.user.service.UserRdbService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@EnableAutoConfiguration
@SpringBootTest(classes = {DeviceTokenRegisterService.class, UserRdbService.class, DeviceTokenRdbService.class})
@EntityScan(basePackageClasses = {User.class, DeviceToken.class})
@EnableJpaRepositories(basePackageClasses = {UserRepository.class, DeviceTokenRepository.class}, repositoryFactoryBeanClass = ExtendedRepositoryFactory.class)
@ActiveProfiles(resolver = DomainServiceIntegrationProfileResolver.class)
@Import(value = {JpaTestConfig.class})
public class DeviceTokenRegisterServiceIntegrationTest extends DomainServiceTestInfraConfig {
    @Autowired
    private DeviceTokenRegisterService deviceTokenRegisterService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(UserFixture.GENERAL_USER.toUser());
    }

    @Test
    @DisplayName("새로운 토큰 등록 요청 시 디바이스 토큰이 정상적으로 저장됩니다")
    void when_registering_new_token_then_save_successfully() {
        // given
        String deviceId = "newDevice";
        String token = "newToken";

        // when
        DeviceToken result = deviceTokenRegisterService.execute(savedUser.getId(), deviceId, "Android", token);

        // then
        // 1. 반환된 결과 검증
        assertEquals(token, result.getToken());
        assertEquals(deviceId, result.getDeviceId());
        assertEquals(savedUser.getId(), result.getUser().getId());
        assertTrue(result.isActivated());

        // 2. 실제 DB 저장 여부 검증
        DeviceToken savedToken = deviceTokenRepository.findById(result.getId())
                .orElseThrow(() -> new IllegalStateException("저장된 토큰을 찾을 수 없습니다."));

        assertEquals(token, savedToken.getToken());
        assertEquals(deviceId, savedToken.getDeviceId());
        assertEquals(savedUser.getId(), savedToken.getUser().getId());
        assertTrue(savedToken.isActivated());
    }

    @Test
    @Transactional
    @DisplayName("사용자가 동일한 디바이스에 새로운 토큰을 등록하면 기존 토큰이 비활성화됩니다")
    void when_registering_new_token_for_same_device_then_deactivate_existing_token() {
        // given
        String deviceId = "device1";
        DeviceToken firstToken = deviceTokenRepository.save(DeviceToken.of("token1", deviceId, "Android", savedUser));

        // when
        DeviceToken secondToken = deviceTokenRegisterService.execute(savedUser.getId(), deviceId, "Android", "token2");

        // then
        assertFalse(firstToken.isActivated());
        assertTrue(secondToken.isActivated());
        assertEquals(deviceId, secondToken.getDeviceId());

        // DB에 실제로 저장되었는지 확인
        List<DeviceToken> savedTokens = deviceTokenRepository.findAllByUser_Id(savedUser.getId());
        assertEquals(2, savedTokens.size());
        assertTrue(savedTokens.stream().filter(DeviceToken::isActivated).count() == 1);
    }

    @Test
    @Transactional
    @DisplayName("활성화된 토큰을 다른 디바이스에서 사용하려고 하면 예외가 발생합니다")
    void when_using_active_token_on_different_device_then_throw_exception() {
        // given
        String token = "token1";
        deviceTokenRepository.save(DeviceToken.of(token, "device1", "iPhone", savedUser));

        // when & then
        DeviceTokenErrorException exception = assertThrowsExactly(
                DeviceTokenErrorException.class,
                () -> deviceTokenRegisterService.execute(savedUser.getId(), "device2", "iPhone", token)
        );
        assertEquals(DeviceTokenErrorCode.DUPLICATED_DEVICE_TOKEN, exception.getBaseErrorCode());
    }

    @Test
    @Transactional
    @DisplayName("같은 deviceId, token / 다른 사용자 갱신 요청이라면, 디바이스 토큰의 소유권이 다른 사용자에게 이전됩니다")
    void shouldTransferTokenOwnership() {
        // given
        User anotherUser = userRepository.save(UserFixture.GENERAL_USER.toUser());

        String deviceId = "device1";
        String token = "token1";

        DeviceToken firstUserToken = deviceTokenRepository.save(DeviceToken.of(token, deviceId, "Android", savedUser));

        // when
        DeviceToken secondUserToken = deviceTokenRegisterService.execute(anotherUser.getId(), deviceId, "Android", token);

        // then
        assertEquals(firstUserToken.getId(), secondUserToken.getId());
        assertEquals(anotherUser.getId(), secondUserToken.getUser().getId());
        assertTrue(secondUserToken.isActivated());
    }

    @Test
    @DisplayName("사용자는 여러 기기에 서로 다른 토큰을 등록할 수 있습니다")
    void when_user_registers_multiple_devices_then_allow_different_tokens() {
        // given
        String device1Token = "token1";
        String device2Token = "token2";

        // when
        DeviceToken result1 = deviceTokenRegisterService.execute(savedUser.getId(), "device1", "Android", device1Token);
        DeviceToken result2 = deviceTokenRegisterService.execute(savedUser.getId(), "device2", "iPhone", device2Token);

        // then
        assertTrue(result1.isActivated());
        assertTrue(result2.isActivated());
        assertNotEquals(result1.getDeviceId(), result2.getDeviceId());
        assertNotEquals(result1.getToken(), result2.getToken());

        // DB에 실제로 저장되었는지 확인
        List<DeviceToken> savedTokens = deviceTokenRepository.findAllByUser_Id(savedUser.getId());
        assertEquals(2, savedTokens.size());
        assertTrue(savedTokens.stream().allMatch(DeviceToken::isActivated));

        deviceTokenRepository.deleteAll(savedTokens);
    }
}
