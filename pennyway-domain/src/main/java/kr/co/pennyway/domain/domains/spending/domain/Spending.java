package kr.co.pennyway.domain.domains.spending.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.common.converter.SpendingIconConverter;
import kr.co.pennyway.domain.common.model.DateAuditable;
import kr.co.pennyway.domain.domains.spending.type.SpendingIcon;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "spending")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE spending SET deleted_at = NOW() WHERE id = ?")
public class Spending extends DateAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer amount;
    @Convert(converter = SpendingIconConverter.class)
    private SpendingIcon category;
    private LocalDateTime spendAt;
    private String accountName;
    private String memo;
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /* category가 OTHER일 경우 spendingCustomCategory를 참조 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spending_custom_category_id")
    private SpendingCustomCategory spendingCustomCategory;

    @Builder
    private Spending(Integer amount, SpendingIcon category, LocalDateTime spendAt, String accountName, String memo, User user, SpendingCustomCategory spendingCustomCategory) {
        this.amount = amount;
        this.category = category;
        this.spendAt = spendAt;
        this.accountName = accountName;
        this.memo = memo;
        this.user = user;
        this.spendingCustomCategory = spendingCustomCategory;
    }
}
