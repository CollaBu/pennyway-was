package kr.co.pennyway.domain.domains.target.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.common.model.DateAuditable;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@Table(name = "target_amount")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE target_amount SET amount = -1 WHERE id = ?")
public class TargetAmount extends DateAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private TargetAmount(int amount, User user) {
        this.amount = amount;
        this.user = user;
    }

    public static TargetAmount of(int amount, User user) {
        return new TargetAmount(amount, user);
    }

    public void updateAmount(Integer amount) {
        this.amount = amount;
    }

    public boolean isAllocatedAmount() {
        return this.amount >= 0;
    }

    @Override
    public String toString() {
        return "TargetAmount(id=" + this.getId() + ", amount=" + this.getAmount() + ")";
    }
}
