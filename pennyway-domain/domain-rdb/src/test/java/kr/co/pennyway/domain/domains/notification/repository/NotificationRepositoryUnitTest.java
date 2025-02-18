package kr.co.pennyway.domain.domains.notification.repository;

import kr.co.pennyway.domain.config.ContainerMySqlTestConfig;
import kr.co.pennyway.domain.config.JpaConfig;
import kr.co.pennyway.domain.config.JpaTestConfig;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.util.AssertionErrors.*;

@Slf4j
@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create"})
@ContextConfiguration(classes = JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaTestConfig.class)
@ActiveProfiles("test")
public class NotificationRepositoryUnitTest extends ContainerMySqlTestConfig {
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

        Notification notification = new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING, user1)
                .build();
        notificationRepository.save(notification);

        // when
        notificationRepository.saveDailySpendingAnnounceInBulk(
                List.of(user1.getId(), user2.getId()),
                Announcement.DAILY_SPENDING
        );

        // then
        List<Notification> notifications = notificationRepository.findAll();
        log.debug("notifications: {}", notifications);
        assertEquals("알림이 중복 저장되지 않아야 한다.", 2, notifications.size());
    }

    @Test
    @DisplayName("사용자의 여러 알림을 읽음 처리할 수 있다.")
    void updateReadAtSuccessfully() {
        // given
        User user = userRepository.save(createUser("jayang"));

        List<Notification> notifications = notificationRepository.saveAll(List.of(
                new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING, user).build(),
                new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING, user).build(),
                new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING, user).build()));

        // when
        notificationRepository.updateReadAtByIdsInBulk(notifications.stream().map(Notification::getId).toList());

        // then
        notificationRepository.findAll().forEach(notification -> {
            log.info("notification: {}", notification);
            assertNotNull("알림이 읽음 처리 되어야 한다.", notification.getReadAt());
        });
    }

    @Test
    @DisplayName("사용자의 읽지 않은 알림 개수를 조회할 수 있다.")
    void countUnreadNotificationsByIds() {
        // given
        User user = userRepository.save(createUser("jayang"));

        List<Notification> notifications = notificationRepository.saveAll(List.of(
                new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING, user).build(),
                new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING, user).build(),
                new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING, user).build()));
        List<Long> ids = notifications.stream().map(Notification::getId).toList();

        notificationRepository.updateReadAtByIdsInBulk(List.of(ids.get(1)));

        // when
        long count = notificationRepository.countUnreadNotificationsByIds(
                user.getId(),
                notifications.stream().map(Notification::getId).toList()
        );

        // then
        assertEquals("읽지 않은 알림 개수가 2개여야 한다.", 2L, count);
    }

    @Test
    @DisplayName("사용자의 읽지 않은 알림이 존재하면 true를 반환한다.")
    void existsTopByReceiver_IdAndReadAtIsNull() {
        // given
        User user = userRepository.save(createUser("jayang"));

        Notification notification1 = new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING, user).build();
        Notification notification2 = new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING, user).build();
        Notification notification3 = new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING, user).build();

        ReflectionTestUtils.setField(notification1, "readAt", LocalDateTime.now());
        ReflectionTestUtils.setField(notification2, "readAt", LocalDateTime.now());

        notificationRepository.saveAll(List.of(notification1, notification2, notification3));

        // when
        boolean exists = notificationRepository.existsUnreadNotification(user.getId());

        // then
        assertTrue("읽지 않은 알림이 존재하면 true를 반환해야 한다.", exists);
    }

    @Test
    @DisplayName("사용자의 읽지 않은 알림이 존재하지 않으면 false를 반환한다.")
    void notExistsTopByReceiver_IdAndReadAtIsNull() {
        // given
        User user = userRepository.save(createUser("jayang"));

        Notification notification1 = new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING, user).build();
        Notification notification2 = new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING, user).build();
        Notification notification3 = new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING, user).build();

        ReflectionTestUtils.setField(notification1, "readAt", LocalDateTime.now());
        ReflectionTestUtils.setField(notification2, "readAt", LocalDateTime.now());
        ReflectionTestUtils.setField(notification3, "readAt", LocalDateTime.now());

        notificationRepository.saveAll(List.of(notification1, notification2, notification3));

        // when
        boolean exists = notificationRepository.existsUnreadNotification(user.getId());

        // then
        assertFalse("읽지 않은 알림이 존재하지 않으면 false를 반환해야 한다.", exists);
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
