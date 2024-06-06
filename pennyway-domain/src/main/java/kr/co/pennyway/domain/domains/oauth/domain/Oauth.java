package kr.co.pennyway.domain.domains.oauth.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.common.converter.ProviderConverter;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Table(name = "oauth")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@DynamicInsert
@SQLDelete(sql = "UPDATE oauth SET deleted_at = NOW() WHERE id = ?")
public class Oauth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = ProviderConverter.class)
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
    private Oauth(Provider provider, String oauthId, User user) {
        if (!StringUtils.hasText(oauthId)) {
            throw new IllegalArgumentException("oauthId는 null이거나 빈 문자열이 될 수 없습니다.");
        }

        this.provider = Objects.requireNonNull(provider, "provider는 null이 될 수 없습니다.");
        this.oauthId = oauthId;
        this.user = Objects.requireNonNull(user, "user는 null이 될 수 없습니다.");
    }

    public static Oauth of(Provider provider, String oauthId, User user) {
        return Oauth.builder()
                .provider(provider)
                .oauthId(oauthId)
                .user(user)
                .build();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void revertDelete(String oauthId) {
        if (deletedAt == null) {
            throw new IllegalStateException("삭제되지 않은 oauth 정보 갱신 요청입니다. oauthId: " + oauthId);
        }
        if (!StringUtils.hasText(oauthId)) {
            throw new IllegalArgumentException("oauthId는 null이거나 빈 문자열이 될 수 없습니다.");
        }

        this.oauthId = oauthId;
        this.deletedAt = null;
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
