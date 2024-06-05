package kr.co.pennyway.domain.domains.spending.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.common.converter.SpendingCategoryConverter;
import kr.co.pennyway.domain.common.model.DateAuditable;
import kr.co.pennyway.domain.domains.spending.dto.CategoryInfo;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "spending")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE spending SET deleted_at = NOW() WHERE id = ?")
public class Spending extends DateAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer amount;
    @Convert(converter = SpendingCategoryConverter.class)
    private SpendingCategory category;
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
    private Spending(Integer amount, SpendingCategory category, LocalDateTime spendAt, String accountName, String memo, User user, SpendingCustomCategory spendingCustomCategory) {
        if (category.equals(SpendingCategory.CUSTOM) && spendingCustomCategory == null) {
            throw new IllegalArgumentException("OTHER 아이콘의 경우 SpendingCustomCategory는 null일 수 없습니다.");
        } else if (!category.equals(SpendingCategory.CUSTOM) && spendingCustomCategory != null) {
            throw new IllegalArgumentException("OTHER 아이콘이 아닌 경우 SpendingCustomCategory는 null이어야 합니다.");
        }

        this.amount = amount;
        this.category = category;
        this.spendAt = spendAt;
        this.accountName = accountName;
        this.memo = memo;
        this.user = user;
        this.spendingCustomCategory = spendingCustomCategory;
    }

    public int getDay() {
        return spendAt.getDayOfMonth();
    }

    /**
     * 지출 내역의 소비 카테고리를 조회하는 메서드 <br>
     * SpendingCategory가 OTHER일 경우 SpendingCustomCategory를 정보를 조회하여 반환한다.
     *
     * @return {@link CategoryInfo}
     */
    public CategoryInfo getCategory() {
        if (this.category.equals(SpendingCategory.CUSTOM)) {
            SpendingCustomCategory category = getSpendingCustomCategory();
            return CategoryInfo.of(category.getId(), category.getName(), category.getIcon());
        }

        return CategoryInfo.of(-1L, this.category.getType(), this.category);
    }

    public void updateSpendingCustomCategory(SpendingCustomCategory spendingCustomCategory) {
        if (this.category.equals(SpendingCategory.CUSTOM) && spendingCustomCategory == null) {
            throw new IllegalArgumentException("OTHER 아이콘의 경우 SpendingCustomCategory는 null일 수 없습니다.");
        } else if (!this.category.equals(SpendingCategory.CUSTOM) && spendingCustomCategory != null) {
            throw new IllegalArgumentException("OTHER 아이콘이 아닌 경우 SpendingCustomCategory는 null이어야 합니다.");
        }

        this.spendingCustomCategory = spendingCustomCategory;
    }

    public void update(Spending spending) {
        this.amount = spending.amount;
        this.category = spending.category;
        this.spendAt = spending.spendAt;
        this.accountName = spending.accountName;
        this.memo = spending.memo;
        this.spendingCustomCategory = spending.spendingCustomCategory;
    }
}
