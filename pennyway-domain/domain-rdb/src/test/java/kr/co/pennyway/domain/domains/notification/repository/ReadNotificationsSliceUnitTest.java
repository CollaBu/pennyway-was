package kr.co.pennyway.domain.domains.notification.repository;

import kr.co.pennyway.domain.config.ContainerMySqlTestConfig;
import kr.co.pennyway.domain.config.JpaConfig;
import kr.co.pennyway.domain.config.JpaTestConfig;
import kr.co.pennyway.domain.domains.notification.domain.Notification;
import kr.co.pennyway.domain.domains.notification.service.NotificationRdbService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@Slf4j
@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create"})
@ContextConfiguration(classes = {JpaConfig.class, NotificationRdbService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaTestConfig.class)
@ActiveProfiles("test")
public class ReadNotificationsSliceUnitTest extends ContainerMySqlTestConfig {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRdbService notificationService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    @DisplayName("특정 사용자의 알림 목록을 슬라이스로 조회하며, 결과는 최신순으로 정렬되어야 한다.")
    public void readNotificationsSliceSorted() {
        // given
        User user = userRepository.save(createUser("jayang"));
        Pageable pa = PageRequest.of(0, 5, Sort.by(Sort.Order.desc("notification.createdAt")));

        List<Notification> notifications = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Notification notification = new Notification.Builder(NoticeType.ANNOUNCEMENT, Announcement.DAILY_SPENDING, user).build();
            ReflectionTestUtils.setField(notification, "readAt", LocalDateTime.now());
            notifications.add(notification);
        }
        bulkInsertNotifications(notifications);

        // when
        Slice<Notification> result = notificationService.readNotificationsSlice(user.getId(), pa, NoticeType.ANNOUNCEMENT);

        // then
        assertEquals("Slice 데이터 개수는 5개여야 한다.", 5, result.getNumberOfElements());
        assertTrue("hasNext()는 true여야 한다.", result.hasNext());
        for (int i = 0; i < result.getNumberOfElements() - 1; i++) {
            Notification current = result.getContent().get(i);
            Notification next = result.getContent().get(i + 1);
            log.debug("current: {}, next: {}", current.getCreatedAt(), next.getCreatedAt());
            log.debug("notification: {}", current);
            assert current.getCreatedAt().isAfter(next.getCreatedAt());
        }
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

    private void bulkInsertNotifications(List<Notification> notifications) {
        String sql = String.format("""
                INSERT INTO `%s` (type, announcement, created_at, updated_at, receiver, receiver_name, read_at)
                VALUES (:type, :announcement, :createdAt, :updatedAt, :receiver, :receiverName, :readAt);
                """, "notification");

        LocalDateTime date = LocalDateTime.now();
        SqlParameterSource[] params = new SqlParameterSource[notifications.size()];

        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            params[i] = new MapSqlParameterSource()
                    .addValue("type", notification.getType().getCode())
                    .addValue("announcement", notification.getAnnouncement().getCode())
                    .addValue("createdAt", date)
                    .addValue("updatedAt", date)
                    .addValue("receiver", notification.getReceiver().getId())
                    .addValue("receiverName", notification.getReceiverName())
                    .addValue("readAt", notification.getReadAt());
            date = date.minusDays(1);
        }

        jdbcTemplate.batchUpdate(sql, params);
    }
}
