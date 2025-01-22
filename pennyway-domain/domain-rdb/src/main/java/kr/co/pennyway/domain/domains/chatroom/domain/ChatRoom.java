package kr.co.pennyway.domain.domains.chatroom.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import kr.co.pennyway.domain.common.model.DateAuditable;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Table(name = "chat_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE chat_room SET deleted_at = NOW() WHERE id = ?")
public class ChatRoom extends DateAuditable {
    @Id
    private Long id;

    private String title;
    private String description;
    private String backgroundImageUrl;
    private Integer password;

    @ColumnDefault("NULL")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "chatRoom")
    @SQLRestriction("deleted_at IS NULL")
    private List<ChatMember> chatMembers = new ArrayList<>();

    @Builder
    public ChatRoom(Long id, String title, String description, String backgroundImageUrl, Integer password) {
        validate(id, title, description, password);

        this.id = id;
        this.title = title;
        this.description = description;
        this.backgroundImageUrl = backgroundImageUrl;
        this.password = password;
    }

    public void update(String title, String description, String backgroundImageUrl, Integer password) {
        validate(title, description, password);

        this.title = title;
        this.description = description;
        this.backgroundImageUrl = backgroundImageUrl;
        this.password = password;
    }

    private void validate(Long id, String title, String description, Integer password) {
        Objects.requireNonNull(id, "채팅방 ID는 null일 수 없습니다.");

        validate(title, description, password);
    }

    private void validate(String title, String description, Integer password) {
        if (!StringUtils.hasText(title) || title.length() > 50) {
            throw new IllegalArgumentException("제목은 null이거나 빈 문자열이 될 수 없으며, 50자 이하로 제한됩니다.");
        }

        if (description != null && description.length() > 100) {
            throw new IllegalArgumentException("설명은 null이거나 빈 문자열이 될 수 있으며, 100자 이하로 제한됩니다.");
        }

        if (password != null && password < 0 && password.toString().length() != 6) {
            throw new IllegalArgumentException("비밀번호는 null이거나, 6자리 정수여야 하며, 음수는 허용하지 않습니다.");
        }
    }

    public boolean isPrivateRoom() {
        return password != null;
    }

    public boolean matchPassword(Integer password) {
        return this.password.equals(password);
    }

    public boolean hasOnlyAdmin() {
        return Hibernate.size(chatMembers) == 1 && chatMembers.get(0).isAdmin();
    }

    @Override
    public String toString() {
        return "ChatRoom{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", backgroundImageUrl='" + backgroundImageUrl + '\'' +
                ", password=" + password +
                ", deletedAt=" + deletedAt +
                '}';
    }
}
