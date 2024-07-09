package kr.co.pennyway.domain.domains.notification.repository;

import kr.co.pennyway.domain.domains.notification.type.Announcement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationCustomRepositoryImpl implements NotificationCustomRepository {
    private final JdbcTemplate jdbcTemplate;

    private int batchSize = 500;

    @Override
    public void saveDailySpendingAnnounceInBulk(List<Long> userIds, LocalDateTime publishedAt, Announcement announcement) {
        int batchCount = 0;
        List<Long> subItems = new ArrayList<>();

        for (int i = 0; i < userIds.size(); ++i) {
            subItems.add(userIds.get(i));

            if ((i + 1) % batchSize == 0) {
                batchCount = batchInsert(batchCount, subItems, publishedAt, announcement);
            }
        }

        if (!subItems.isEmpty()) {
            batchInsert(batchCount, subItems, publishedAt, announcement);
        }

        log.info("Notification saved. announcement: {}, count: {}", announcement, userIds.size());
    }

    private int batchInsert(int batchCount, List<Long> userIds, LocalDateTime publishedAt, Announcement announcement) {
        String sql = "INSERT INTO notification(id, type, read_at, created_at, updated_at, receiver, announcement) " +
                "SELECT NULL, '0', NULL, NOW(), NOW(), u.id, ? " +
                "FROM user u " +
                "WHERE u.id IN (?) " +
                "AND NOT EXISTS ( " +
                "	SELECT n.receiver " +
                "	FROM notification n " +
                "	WHERE n.receiver = u.id " +
                "    AND n.created_at >= CURDATE() " +
                "    AND n.created_at < CURDATE() + INTERVAL 1 DAY " +
                "	AND n.type = '0' " +
                "	AND n.announcement = ? " +
                ");";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, announcement.getCode());
                ps.setLong(2, userIds.get(i));
                ps.setString(3, announcement.getCode());
            }

            @Override
            public int getBatchSize() {
                return userIds.size();
            }
        });

        userIds.clear();
        return ++batchCount;
    }
}
