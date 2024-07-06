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
    @JoinColumn(name = "receiver")
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender")
    private User sender;

    private Notification(LocalDateTime readAt, NoticeType type, Announcement announcement, User receiver, User sender) {
        this.readAt = Objects.requireNonNull(readAt);
        this.type = Objects.requireNonNull(type);
        this.announcement = Objects.requireNonNull(announcement);
        this.receiver = receiver;
        this.sender = sender;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", readAt=" + readAt +
                ", type=" + type +
                ", announcement=" + announcement +
                '}';
    }

    public static class Builder {
        private Long id;
        private LocalDateTime readAt;
        private NoticeType type;
        private Announcement announcement;

        private User receiver = null;
        private User sender = null;

        public Builder(Long id, LocalDateTime readAt, NoticeType type, Announcement announcement) {
            this.id = id;
            this.readAt = readAt;
            this.type = type;
            this.announcement = announcement;
        }

        public Builder receiver(User receiver) {
            this.receiver = receiver;
            return this;
        }

        public Builder sender(User sender) {
            this.sender = sender;
            return this;
        }

        public Notification build() {
            return new Notification(readAt, type, announcement, receiver, sender);
        }
    }
}
