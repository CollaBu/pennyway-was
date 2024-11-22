package kr.co.pennyway.domain.domains.user.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.common.converter.ProfileVisibilityConverter;
import kr.co.pennyway.domain.common.converter.RoleConverter;
import kr.co.pennyway.domain.common.model.DateAuditable;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import kr.co.pennyway.domain.domains.user.type.Role;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE user SET deleted_at = NOW() WHERE id = ?")
public class User extends DateAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String name;
    @ColumnDefault("NULL")
    private String password;
    @ColumnDefault("NULL")
    private LocalDateTime passwordUpdatedAt;
    @ColumnDefault("NULL")
    private String profileImageUrl;
    private String phone;
    @Convert(converter = RoleConverter.class)
    private Role role;
    @Convert(converter = ProfileVisibilityConverter.class)
    private ProfileVisibility profileVisibility;
    @ColumnDefault("false")
    private boolean locked;
    @Embedded
    private NotifySetting notifySetting;
    @ColumnDefault("NULL")
    private LocalDateTime deletedAt;

    @Builder
    private User(String username, String name, String password, LocalDateTime passwordUpdatedAt, String profileImageUrl, String phone, Role role,
                 ProfileVisibility profileVisibility, NotifySetting notifySetting, boolean locked) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("username은 null이거나 빈 문자열이 될 수 없습니다.");
        } else if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("name은 null이거나 빈 문자열이 될 수 없습니다.");
        }

        this.username = username;
        this.name = name;
        this.password = password;
        this.passwordUpdatedAt = passwordUpdatedAt;
        this.profileImageUrl = profileImageUrl;
        this.phone = Objects.requireNonNull(phone, "phone은 null이 될 수 없습니다.");
        this.role = Objects.requireNonNull(role, "role은 null이 될 수 없습니다.");
        this.profileVisibility = Objects.requireNonNull(profileVisibility, "profileVisibility는 null이 될 수 없습니다.");
        this.notifySetting = Objects.requireNonNull(notifySetting, "notifySetting은 null이 될 수 없습니다.");
        this.locked = locked;
    }

    public void updatePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("password는 null이거나 빈 문자열이 될 수 없습니다.");
        }

        this.password = password;
        this.passwordUpdatedAt = LocalDateTime.now();
    }

    public void updateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("name은 null이거나 빈 문자열이 될 수 없습니다.");
        }

        this.name = name;
    }

    public void updateUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("username은 null이거나 빈 문자열이 될 수 없습니다.");
        }

        this.username = username;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }

    public boolean isGeneralSignedUpUser() {
        return password != null;
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", deletedAt=" + deletedAt +
                '}';
    }
}
