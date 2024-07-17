package kr.co.pennyway.domain.domains.notification.repository;

import kr.co.pennyway.domain.domains.notification.type.Announcement;
import kr.co.pennyway.domain.domains.notification.type.NoticeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationCustomRepositoryImpl implements NotificationCustomRepository {
    private final JdbcTemplate jdbcTemplate;

    private final int BATCH_SIZE = 500;

    @Override
    public void saveDailySpendingAnnounceInBulk(List<Long> userIds, Announcement announcement) {
        int batchCount = 0;
        List<Long> subItems = new ArrayList<>();

        for (int i = 0; i < userIds.size(); ++i) {
            subItems.add(userIds.get(i));

            if ((i + 1) % BATCH_SIZE == 0) {
                batchCount = batchInsert(batchCount, subItems, NoticeType.ANNOUNCEMENT, announcement);
            }
        }

        if (!subItems.isEmpty()) {
            batchInsert(batchCount, subItems, NoticeType.ANNOUNCEMENT, announcement);
        }

        log.info("Notification saved. announcement: {}, count: {}", announcement, userIds.size());
    }

    private int batchInsert(int batchCount, List<Long> userIds, NoticeType noticeType, Announcement announcement) {
        String sql = "INSERT INTO notification(id, read_at, type, announcement, created_at, updated_at, receiver, receiver_name) " +
                "SELECT NULL, NULL, ?, ?, NOW(), NOW(), u.id, u.name " +
                "FROM user u " +
                "WHERE u.id IN (?) " +
                "AND NOT EXISTS ( " +
                "	SELECT n.receiver " +
                "	FROM notification n " +
                "	WHERE n.receiver = u.id " +
                "    AND n.created_at >= CURDATE() " +
                "    AND n.created_at < CURDATE() + INTERVAL 1 DAY " +
                "	AND n.type = ? " +
                "	AND n.announcement = ? " +
                ");";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, noticeType.getCode());
                ps.setString(2, announcement.getCode());
                ps.setLong(3, userIds.get(i));
                ps.setString(4, noticeType.getCode());
                ps.setString(5, announcement.getCode());
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
