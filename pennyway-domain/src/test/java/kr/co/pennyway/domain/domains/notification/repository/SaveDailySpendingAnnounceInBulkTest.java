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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.util.AssertionErrors.assertEquals;

@Slf4j
@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create"})
@ContextConfiguration(classes = JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfig.class)
@ActiveProfiles("test")
public class SaveDailySpendingAnnounceInBulkTest extends ContainerMySqlTestConfig {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @Transactional
    @DisplayName("여러 사용자에게 일일 소비 알림을 저장할 수 있다.")
    public void saveDailySpendingAnnounceInBulk() {
        // given
        User user1 = userRepository.save(createUser("jayang"));
        User user2 = userRepository.save(createUser("mock"));
        User user3 = userRepository.save(createUser("test"));

        // when
        notificationRepository.saveDailySpendingAnnounceInBulk(
                List.of(user1.getId(), user2.getId(), user3.getId()),
                LocalDateTime.now(),
                Announcement.DAILY_SPENDING
        );

        // then
        notificationRepository.findAll().forEach(notification -> {
            log.info("notification: {}", notification);
            assertEquals("알림 타입이 일일 소비 알림이어야 한다.", Announcement.DAILY_SPENDING, notification.getAnnouncement());
        });
    }

    @Test
    @Transactional
    @DisplayName("이미 당일에 알림을 받은 사용자에게 데이터가 중복 저장되지 않아야 한다.")
    public void notSaveDuplicateNotification() {
        // given
        User user1 = userRepository.save(createUser("jayang"));
        User user2 = userRepository.save(createUser("mock"));

        Notification notification = new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING)
                .receiver(user1)
                .build();
        notificationRepository.save(notification);

        // when
        notificationRepository.saveDailySpendingAnnounceInBulk(
                List.of(user1.getId(), user2.getId()),
                LocalDateTime.now(),
                Announcement.DAILY_SPENDING
        );

        // then
        List<Notification> notifications = notificationRepository.findAll();
        assertEquals("알림이 중복 저장되지 않아야 한다.", 2, notifications.size());
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
