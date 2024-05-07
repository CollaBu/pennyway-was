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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private SpendingCategory spendingCategory;

    @Builder
    private Spending(Integer amount, SpendingIcon category, LocalDateTime spendAt, String accountName, String memo, User user, SpendingCategory spendingCategory) {
        this.amount = amount;
        this.category = category;
        this.spendAt = spendAt;
        this.accountName = accountName;
        this.memo = memo;
        this.user = user;
        this.spendingCategory = spendingCategory;
    }
}
