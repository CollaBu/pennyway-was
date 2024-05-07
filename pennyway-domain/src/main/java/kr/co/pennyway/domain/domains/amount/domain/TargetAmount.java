package kr.co.pennyway.domain.domains.amount.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.common.model.DateAuditable;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "target_amount")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE target_amount SET deleted_at = NOW() WHERE id = ?")
public class TargetAmount extends DateAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer amount;
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private TargetAmount(Integer amount, User user) {
        this.amount = amount;
        this.user = user;
    }

    public static TargetAmount of(Integer amount, User user) {
        return new TargetAmount(amount, user);
    }
}
