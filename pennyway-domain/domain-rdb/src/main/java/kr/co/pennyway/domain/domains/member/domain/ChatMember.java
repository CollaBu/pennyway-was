package kr.co.pennyway.domain.domains.member.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.common.converter.ChatMemberRoleConverter;
import kr.co.pennyway.domain.common.model.DateAuditable;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Table(name = "chat_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@SQLDelete(sql = "UPDATE chat_member SET deleted_at = NOW() WHERE id = ?")
public class ChatMember extends DateAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = ChatMemberRoleConverter.class)
    private ChatMemberRole role;

    @ColumnDefault("false")
    private boolean banned;
    @ColumnDefault("true")
    private boolean notifyEnabled;

    @ColumnDefault("NULL")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @Builder
    protected ChatMember(User user, ChatRoom chatRoom, ChatMemberRole role) {
        validate(user, chatRoom, role);

        this.user = user;
        this.chatRoom = chatRoom;
        this.role = role;
        this.notifyEnabled = true;
    }

    public static ChatMember of(User user, ChatRoom chatRoom, ChatMemberRole role) {
        return ChatMember.builder()
                .user(user)
                .chatRoom(chatRoom)
                .role(role)
                .build();
    }

    private void validate(User user, ChatRoom chatRoom, ChatMemberRole role) {
        Objects.requireNonNull(user, "user는 null이 될 수 없습니다.");
        Objects.requireNonNull(chatRoom, "chatRoom은 null이 될 수 없습니다.");
        Objects.requireNonNull(role, "role은 null이 될 수 없습니다.");
    }

    /**
     * 사용자 데이터가 삭제되었는지 확인한다.
     *
     * @return 삭제된 데이터가 아니면 true, 삭제된 데이터이면 false
     */
    public boolean isActive() {
        return deletedAt == null;
    }

    /**
     * 사용자 추방된 이력이 있는 지 확인한다.
     *
     * @return 추방된 이력이 있으면 true, 없으면 false
     */
    public boolean isBannedMember() {
        return deletedAt != null && banned;
    }

    public void enableNotify() {
        this.notifyEnabled = true;
    }

    public void disableNotify() {
        this.notifyEnabled = false;
    }

    public void ban() {
        this.banned = true;
        this.deletedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "ChatMember{" +
                "id=" + id +
                ", banned=" + banned +
                ", notifyEnabled=" + notifyEnabled +
                ", deletedAt=" + deletedAt +
                '}';
    }
}
