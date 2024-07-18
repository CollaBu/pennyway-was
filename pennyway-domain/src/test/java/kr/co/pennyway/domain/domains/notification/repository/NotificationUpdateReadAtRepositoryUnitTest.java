package kr.co.pennyway.domain.domains.notification.repository;

import kr.co.pennyway.domain.config.ContainerMySqlTestConfig;
import kr.co.pennyway.domain.config.JpaConfig;
import kr.co.pennyway.domain.config.TestJpaConfig;
import kr.co.pennyway.domain.domains.notification.domain.Notification;
import kr.co.pennyway.domain.domains.notification.type.Announcement;
import kr.co.pennyway.domain.domains.notification.type.NoticeType;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.springframework.test.util.AssertionErrors.assertNotNull;

@Slf4j
@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create"})
@ContextConfiguration(classes = JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfig.class)
@ActiveProfiles("test")
public class NotificationUpdateReadAtRepositoryUnitTest extends ContainerMySqlTestConfig {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("사용자의 여러 알림을 읽음 처리할 수 있다.")
    void updateReadAtSuccessfully() {
        // given
        User user = userRepository.save(createUser("jayang"));

        notificationRepository.saveAll(List.of(
                new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING, user).build(),
                new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING, user).build(),
                new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING, user).build()));

        // when
        notificationRepository.updateReadAtByIds(List.of(1L, 2L, 3L));

        // then
        notificationRepository.findAll().forEach(notification -> {
            log.info("notification: {}", notification);
            assertNotNull("알림이 읽음 처리 되어야 한다.", notification.getReadAt());
        });
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
