package kr.co.pennyway.domain.domains.notification.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.common.converter.AnnouncementConverter;
import kr.co.pennyway.domain.common.converter.NoticeTypeConverter;
import kr.co.pennyway.domain.common.model.DateAuditable;
import kr.co.pennyway.domain.domains.notification.type.Announcement;
import kr.co.pennyway.domain.domains.notification.type.NoticeType;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "notification")
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
}
