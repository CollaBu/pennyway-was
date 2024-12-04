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
    @Transactional
    @DisplayName("디바이스 토큰 등록 시 기존 활성 토큰은 비활성화됩니다")
    void shouldDeactivateExistingTokensWhenRegisteringNew() {
        // given
        String deviceId = "device1";

        // when
        DeviceToken firstToken = deviceTokenRegisterService.execute(savedUser.getId(), deviceId, "Android", "token1");
        DeviceToken secondToken = deviceTokenRegisterService.execute(savedUser.getId(), deviceId, "Android", "token2");

        // then
        assertFalse(firstToken.isActivated());
        assertTrue(secondToken.isActivated());
    }

    @Test
    @Transactional
    @DisplayName("활성화된 토큰이 다른 디바이스에서 사용되면 예외가 발생합니다")
    void shouldThrowExceptionWhenActiveTokenIsUsedOnDifferentDevice() {
        // given
        String token = "token1";
        deviceTokenRegisterService.execute(savedUser.getId(), "device1", "Android", token);

        // when & then
        DeviceTokenErrorException exception = assertThrowsExactly(
                DeviceTokenErrorException.class,
                () -> deviceTokenRegisterService.execute(savedUser.getId(), "device2", "iPhone", token)
        );
        assertEquals(DeviceTokenErrorCode.DUPLICATED_DEVICE_TOKEN, exception.getBaseErrorCode());
    }

    @Test
    @DisplayName("같은 deviceId, token / 다른 사용자 갱신 요청이라면, 디바이스 토큰의 소유권이 다른 사용자에게 이전됩니다")
    void shouldTransferTokenOwnership() {
        // given
        User anotherUser = userRepository.save(UserFixture.GENERAL_USER.toUser());

        String deviceId = "device1";
        String token = "token1";

        // when
        DeviceToken firstUserToken = deviceTokenRegisterService.execute(savedUser.getId(), deviceId, "Android", token);
        DeviceToken secondUserToken = deviceTokenRegisterService.execute(anotherUser.getId(), deviceId, "Android", token);

        // then
        assertEquals(firstUserToken.getId(), secondUserToken.getId());
        assertEquals(anotherUser.getId(), secondUserToken.getUser().getId());
        assertTrue(secondUserToken.isActivated());
    }
}
