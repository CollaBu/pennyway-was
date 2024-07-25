package kr.co.pennyway.domain.domains.notification.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.common.converter.AnnouncementConverter;
import kr.co.pennyway.domain.common.converter.NoticeTypeConverter;
import kr.co.pennyway.domain.common.model.DateAuditable;
import kr.co.pennyway.domain.domains.notification.type.Announcement;
import kr.co.pennyway.domain.domains.notification.type.NoticeType;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends DateAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime readAt;
    @Convert(converter = NoticeTypeConverter.class)
    private NoticeType type;
    @Convert(converter = AnnouncementConverter.class)
    private Announcement announcement; // 공지 종류

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender")
    private User sender;
    private String senderName;

    private Long toId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver")
    private User receiver;
    private String receiverName;

    private Notification(NoticeType type, Announcement announcement, User sender, String senderName, Long toId, User receiver, String receiverName) {
        this.type = Objects.requireNonNull(type);
        this.announcement = Objects.requireNonNull(announcement);
        this.sender = (!type.equals(NoticeType.ANNOUNCEMENT)) ? Objects.requireNonNull(sender) : sender;
        this.senderName = (!type.equals(NoticeType.ANNOUNCEMENT)) ? Objects.requireNonNull(senderName) : senderName;
        this.toId = toId;
        this.receiver = Objects.requireNonNull(receiver);
        this.receiverName = Objects.requireNonNull(receiverName);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", readAt=" + readAt +
                ", type=" + type +
                ", announcement=" + announcement +
                ", senderName='" + senderName + '\'' +
                ", toId=" + toId +
                ", receiverName='" + receiverName + '\'' +
                '}';
    }

    /**
     * 공지 제목을 생성한다.
     * <br>
     * 이 메서드는 내부적으로 알림 타입의 종류에 따라 공지 제목을 포맷팅한다.
     *
     * @apiNote 이 메서드는 {@link NoticeType#ANNOUNCEMENT} 타입에 대해서만 동작한다. 다른 타입의 알림을 포맷팅해야 하는 경우 해당 메서드를 확장해야 한다.
     */
    public String createFormattedTitle() {
        if (!type.equals(NoticeType.ANNOUNCEMENT)) {
            return ""; // TODO: 알림 종류가 신규로 추가될 때, 해당 로직을 구현해야 함.
        }

        return formatAnnouncementTitle();
    }

    private String formatAnnouncementTitle() {
        if (announcement.equals(Announcement.MONTHLY_TARGET_AMOUNT)) {
            return announcement.createFormattedTitle(String.valueOf(getCreatedAt().getMonthValue()));
        }

        return announcement.createFormattedTitle(receiverName);
    }

    /**
     * 공지 내용을 생성한다.
     * <br>
     * 이 메서드는 내부적으로 알림 타입의 종류에 따라 공지 내용을 포맷팅한다.
     *
     * @apiNote 이 메서드는 {@link NoticeType#ANNOUNCEMENT} 타입에 대해서만 동작한다. 다른 타입의 알림을 포맷팅해야 하는 경우 해당 메서드를 확장해야 한다.
     */
    public String createFormattedContent() {
        if (!type.equals(NoticeType.ANNOUNCEMENT)) {
            return ""; // TODO: 알림 종류가 신규로 추가될 때, 해당 로직을 구현해야 함.
        }

        return announcement.createFormattedContent(receiverName);
    }

    public static class Builder {
        private final NoticeType type;
        private final Announcement announcement;
        private final User receiver;
        private final String receiverName;

        private User sender;
        private String senderName;

        private Long toId;

        public Builder(NoticeType type, Announcement announcement, User receiver) {
            this.type = type;
            this.announcement = announcement;
            this.receiver = receiver;
            this.receiverName = receiver.getName();
        }

        public Builder sender(User sender) {
            this.sender = sender;
            this.senderName = sender.getName();
            return this;
        }

        public Builder toId(Long toId) {
            this.toId = toId;
            return this;
        }

        public Notification build() {
            return new Notification(type, announcement, sender, senderName, toId, receiver, receiverName);
        }
    }
}
