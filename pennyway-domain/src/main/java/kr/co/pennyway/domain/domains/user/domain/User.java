package kr.co.pennyway.domain.domains.user.domain;

import jakarta.persistence.*;
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

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
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
    @Convert(converter = ProfileVisibility.class)
    private ProfileVisibility profileVisibility;
    @ColumnDefault("false")
    private Boolean locked;
    @Embedded
    private NotifySetting notifySetting;
    @ColumnDefault("NULL")
    private LocalDateTime deletedAt;

    @Builder
    private User(String username, String name, String password, LocalDateTime passwordUpdatedAt, String profileImageUrl, String phone, Role role, ProfileVisibility profileVisibility, NotifySetting notifySetting, Boolean locked, LocalDateTime deletedAt) {
        this.username = username;
        this.name = name;
        this.password = password;
        this.passwordUpdatedAt = passwordUpdatedAt;
        this.profileImageUrl = profileImageUrl;
        this.phone = phone;
        this.role = role;
        this.profileVisibility = profileVisibility;
        this.notifySetting = notifySetting;
        this.locked = locked;
        this.deletedAt = deletedAt;
    }
}
