package kr.co.pennyway.domain.domains.oauth.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "oauth")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@DynamicInsert
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE oauth SET deleted_at = NOW() WHERE id = ?")
public class Oauth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Provider provider;
    private String oauthId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ColumnDefault("NULL")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private Oauth(Provider provider, String oauthId, LocalDateTime createdAt, LocalDateTime deletedAt, User user) {
        this.provider = provider;
        this.oauthId = oauthId;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
        this.user = user;
    }

    public static Oauth of(Provider provider, String oauthId, User user) {
        return Oauth.builder()
                .provider(provider)
                .oauthId(oauthId)
                .user(user)
                .build();
    }

    @Override
    public String toString() {
        return "Oauth{" +
                "id=" + id +
                ", provider=" + provider +
                ", oauthId='" + oauthId + '\'' +
                ", createdAt=" + createdAt +
                ", deletedAt=" + deletedAt +
                ", user=" + user +
                '}';
    }
}
