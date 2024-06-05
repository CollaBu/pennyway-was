package kr.co.pennyway.domain.domains.spending.domain;

import jakarta.persistence.*;
import kr.co.pennyway.domain.common.converter.SpendingCategoryConverter;
import kr.co.pennyway.domain.common.model.DateAuditable;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "spending_custom_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE spending_category SET deleted_at = NOW() WHERE id = ?")
public class SpendingCustomCategory extends DateAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Convert(converter = SpendingCategoryConverter.class)
    private SpendingCategory icon;
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private SpendingCustomCategory(String name, SpendingCategory icon, User user) {
        if (icon.equals(SpendingCategory.CUSTOM)) {
            throw new IllegalArgumentException("OTHER 아이콘은 커스텀 카테고리의 icon으로 사용할 수 없습니다.");
        }

        this.name = name;
        this.icon = icon;
        this.user = user;
    }

    public static SpendingCustomCategory of(String name, SpendingCategory icon, User user) {
        return new SpendingCustomCategory(name, icon, user);
    }
}
