package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.api.common.query.SpendingCategoryType;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.dto.TotalSpendingAmount;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorCode;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorException;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpendingSearchService {
    private final SpendingService spendingService;

    @Transactional(readOnly = true)
    public Spending readSpending(Long spendingId) {
        return spendingService.readSpending(spendingId).orElseThrow(() -> new SpendingErrorException(SpendingErrorCode.NOT_FOUND_SPENDING));
    }

    @Transactional(readOnly = true)
    public List<Spending> readSpendingsAtYearAndMonth(Long userId, int year, int month) {
        return spendingService.readSpendings(userId, year, month);
    }

    /**
     * 카테고리에 등록된 지출 내역 리스트를 조회한다.
     *
     * @param categoryId type이 {@link SpendingCategoryType#CUSTOM}이면 커스텀 카테고리 아이디, {@link SpendingCategoryType#DEFAULT}이면 시스템 제공 카테고리 코드로 사용한다.
     * @param type       {@link SpendingCategoryType#CUSTOM}이면 커스텀 카테고리, {@link SpendingCategoryType#DEFAULT}이면 시스템 제공 카테고리에 대한 쿼리를 호출한다.
     * @return 지출 내역 리스트를 {@link Slice}에 담아서 반환한다.
     */
    @Transactional(readOnly = true)
    public Slice<Spending> readSpendingsByCategoryId(Long userId, Long categoryId, Pageable pageable, SpendingCategoryType type) {
        if (type.equals(SpendingCategoryType.CUSTOM)) {
            return spendingService.readSpendingsSliceByCategoryId(userId, categoryId, pageable);
        }

        SpendingCategory spendingCategory = SpendingCategory.fromCode(categoryId.toString());
        return spendingService.readSpendingsSliceByCategory(userId, spendingCategory, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<TotalSpendingAmount> readTotalSpendingAmountByUserIdThatMonth(Long userId, LocalDate date) {
        return spendingService.readTotalSpendingAmountByUserId(userId, date);
    }

    @Transactional(readOnly = true)
    public List<TotalSpendingAmount> readTotalSpendingsAmountByUserId(Long userId) {
        return spendingService.readTotalSpendingsAmountByUserId(userId);
    }
}
