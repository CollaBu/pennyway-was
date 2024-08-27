package kr.co.pennyway.domain.domains.target.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.common.model.DateAuditable;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

import java.time.YearMonth;
import java.util.Objects;

@Entity
@Getter
@Table(name = "target_amount")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE target_amount SET amount = -1, is_read = 1 WHERE id = ?")
public class TargetAmount extends DateAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int amount;
    private boolean isRead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private TargetAmount(int amount, User user) {
        this.amount = amount;
        this.user = Objects.requireNonNull(user, "user는 null이 될 수 없습니다.");
        this.isRead = false;
    }

    /**
     * @param amount 목표 금액은 null을 허용하지 않는다.
     * @param user   사용자는 null을 허용하지 않는다.
     * @throws NullPointerException amount가 null이거나 user가 null일 때
     */
    public static TargetAmount of(int amount, User user) {
        return new TargetAmount(amount, user);
    }

    /**
     * @param amount 변경할 목표 금액은 null을 허용하지 않는다.
     */
    public void updateAmount(int amount) {
        this.amount = amount;
        this.isRead = true;
    }

    public boolean isAllocatedAmount() {
        return this.amount >= 0;
    }

    /**
     * 해당 TargetAmount가 당월 데이터인지 확인한다.
     *
     * @return 당월 데이터라면 true, 아니라면 false
     */
    public boolean isThatMonth() {
        YearMonth yearMonth = YearMonth.now();
        return this.getCreatedAt().getYear() == yearMonth.getYear() && this.getCreatedAt().getMonth() == yearMonth.getMonth();
    }

    @Override
    public String toString() {
        return "TargetAmount(id=" + this.getId() + ", amount=" + this.getAmount() + ", year = " + this.getCreatedAt().getYear() + ", month = " + this.getCreatedAt().getMonthValue() + ")";
    }
}
