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
import org.junit.jupiter.api.DisplayName;
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

import static org.springframework.test.util.AssertionErrors.assertEquals;
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
    @DisplayName("비활성화된 디바이스 토큰을 제외하고, 알림을 허용한 사용자의 활성화된 디바이스 토큰을 조회한다.")
    public void selectActivatedDeviceTokenThatNotifyTrueUser() {
        // given
        User user = userRepository.save(createUser("jayang"));
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
        assertEquals("조회 결과 원소 개수는 2여야 합니다.", owners.getTotalElements(), 2L);
        for (DeviceTokenOwner owner : owners) {
            assertNotEquals("deviceToken2는 비활성화 토큰입니다.", "deviceToken2", owner.deviceTokens());
        }
    }

    @Test
    @Transactional
    @DisplayName("알림을 허용하지 않은 사용자의 활성화된 디바이스 토큰을 조회하지 않는다.")
    public void notSelectNotifyFalseUser() {
        // given
        User activeUser = userRepository.save(createUser("jayang"));
        User deactiveUser = userRepository.save(createUser("mock"));

        List<DeviceToken> deviceTokens = List.of(
                DeviceToken.of("deviceToken1", activeUser),
                DeviceToken.of("deviceToken2", deactiveUser));
        deviceTokens.get(1).deactivate();

        deviceTokenRepository.saveAll(deviceTokens);

        Pageable pageable = Pageable.ofSize(100);

        // when
        Page<DeviceTokenOwner> owners = deviceTokenRepository.findActivatedDeviceTokenOwners(pageable);

        // then
        assertEquals("조회 결과는 하나여야 합니다.", 1L, owners.getTotalElements());
        assertEquals("알림을 허용하지 않은 사용자의 디바이스 토큰은 조회되지 않아야 합니다.", "jayang", owners.getContent().get(0).name());
    }

    @Test
    @Transactional
    @DisplayName("사용자 별로 디바이스 토큰 리스트를 받을 수 있다.")
    public void selectDeviceTokenListByUserId() {
        // given
        User user1 = userRepository.save(createUser("jayang"));
        User user2 = userRepository.save(createUser("mock"));

        List<DeviceToken> deviceTokens = List.of(
                DeviceToken.of("deviceToken1", user1),
                DeviceToken.of("deviceToken2", user1),
                DeviceToken.of("deviceToken3", user1),
                DeviceToken.of("deviceToken4", user2),
                DeviceToken.of("deviceToken5", user2)
        );
        deviceTokenRepository.saveAll(deviceTokens);

        Pageable pageable = Pageable.ofSize(100);

        // when
        Page<DeviceTokenOwner> owners = deviceTokenRepository.findActivatedDeviceTokenOwners(pageable);

        // then
        log.info("owners: {}", owners.getContent());

        assertEquals("전체 결과 개수는 5개여야 합니다.", 5L, owners.getTotalElements());
        assertEquals("jayang의 디바이스 토큰 개수는 3개여야 합니다.", 3, owners.getContent().get(0).deviceTokens().size());
        assertEquals("mock의 디바이스 토큰 개수는 2개여야 합니다.", 2, owners.getContent().get(1).deviceTokens().size());
    }

    private User createUser(String name) {
        return User.builder()
                .username("test")
                .name(name)
                .password("test")
                .phone("010-1234-5678")
                .role(Role.USER)
                .profileVisibility(ProfileVisibility.PUBLIC)
                .notifySetting(NotifySetting.of(true, true, true))
                .build();
    }
}
