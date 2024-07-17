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
