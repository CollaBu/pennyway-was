package kr.co.pennyway.domain.domains.device.repository;

import kr.co.pennyway.domain.config.ContainerMySqlTestConfig;
import kr.co.pennyway.domain.config.JpaConfig;
import kr.co.pennyway.domain.config.TestJpaConfig;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.dto.DeviceTokenOwner;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.repository.UserRepository;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import kr.co.pennyway.domain.domains.user.type.Role;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.util.AssertionErrors.assertNotEquals;

@Slf4j
@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create"})
@ContextConfiguration(classes = JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfig.class)
@ActiveProfiles("test")
public class ActivatedDeviceSearchTest extends ContainerMySqlTestConfig {
    @Autowired
    private DeviceTokenRepository deviceTokenRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    public void test() {
        // given
        User user = userRepository.save(createUser());
        List<DeviceToken> deviceTokens = List.of(
                DeviceToken.of("deviceToken1", user),
                DeviceToken.of("deviceToken2", user),
                DeviceToken.of("deviceToken3", user)
        );
        deviceTokens.get(1).deactivate();
        deviceTokenRepository.saveAll(deviceTokens);
        Pageable pageable = Pageable.ofSize(100);

        // when
        Page<DeviceTokenOwner> owners = deviceTokenRepository.findActivatedDeviceTokenOwners(pageable);

        // then
        for (DeviceTokenOwner owner : owners) {
            assertNotEquals("deviceToken2는 비활성화 토큰입니다.", "deviceToken2", owner.deviceTokens());
        }
    }

    private User createUser() {
        return User.builder()
                .username("test")
                .name("pannyway")
                .password("test")
                .phone("010-1234-5678")
                .role(Role.USER)
                .profileVisibility(ProfileVisibility.PUBLIC)
                .notifySetting(NotifySetting.of(true, true, true))
                .build();
    }
}
