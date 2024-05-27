package kr.co.pennyway.domain.domains.coupon;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TestCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    /**
     * 사용 가능 재고수량
     */
    private long availableStock;

    public TestCoupon(String name, long availableStock) {
        this.name = name;
        this.availableStock = availableStock;
    }

    public void decreaseStock() {
        validateStock();
        this.availableStock--;
    }

    private void validateStock() {
        if (availableStock < 1) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
    }
}
