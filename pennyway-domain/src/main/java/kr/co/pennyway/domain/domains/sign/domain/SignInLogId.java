package kr.co.pennyway.domain.domains.sign.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class SignInLogId {
    @Transient
    private static final long serialVersionUID = 1L;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;
    @Column(name = "id")
    private Long id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SignInLogId that)) return false;
        return signedAt.equals(that.signedAt) && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return signedAt.hashCode() + id.hashCode();
    }
}
