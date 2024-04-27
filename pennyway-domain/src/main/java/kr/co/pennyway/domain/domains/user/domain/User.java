package kr.co.pennyway.domain.domains.user.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.common.converter.ProfileVisibilityConverter;
import kr.co.pennyway.domain.common.converter.RoleConverter;
import kr.co.pennyway.domain.common.model.DateAuditable;
import kr.co.pennyway.domain.domains.device.domain.Device;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private Boolean locked;
    @Embedded
    private NotifySetting notifySetting;
    @ColumnDefault("NULL")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Device> devices = new ArrayList<>();

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

    public void updatePassword(String password) {
        this.password = password;
        this.passwordUpdatedAt = LocalDateTime.now();
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public boolean isGeneralSignedUpUser() {
        return password != null;
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
