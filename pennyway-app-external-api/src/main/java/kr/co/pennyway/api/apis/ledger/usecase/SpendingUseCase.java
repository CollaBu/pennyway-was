package kr.co.pennyway.api.apis.ledger.usecase;

import kr.co.pennyway.api.apis.ledger.dto.SpendingReq;
import kr.co.pennyway.api.apis.ledger.dto.SpendingSearchRes;
import kr.co.pennyway.api.apis.ledger.mapper.SpendingMapper;
import kr.co.pennyway.api.apis.ledger.service.SpendingSearchService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorException;
import kr.co.pennyway.domain.domains.spending.service.SpendingCustomCategoryService;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SpendingUseCase {
    private final SpendingSearchService spendingSearchService;

    private final UserService userService;
    private final SpendingService spendingService;
    private final SpendingCustomCategoryService spendingCustomCategoryService;

    @Transactional
    public SpendingSearchRes.Individual createSpending(Long userId, SpendingReq request) {
        User user = userService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));

        Spending spending = request.toEntity(user);
        if (request.categoryId().equals(-1L)) {
            spendingService.createSpending(spending);
        } else {
            SpendingCustomCategory customCategory = spendingCustomCategoryService.readSpendingCustomCategory(request.categoryId())
                    .orElseThrow(() -> new SpendingErrorException(SpendingErrorCode.NOT_FOUND_CUSTOM_CATEGORY));

            spending.updateSpendingCustomCategory(customCategory);
            spendingService.createSpending(spending);
        }

        return SpendingMapper.toSpendingSearchResIndividual(spending);
    }

    @Transactional(readOnly = true)
    public SpendingSearchRes.Month getSpendingsAtYearAndMonth(Long userId, int year, int month) {
        List<Spending> spendings = spendingSearchService.readSpendings(userId, year, month);

        return SpendingMapper.toSpendingSearchResMonth(spendings, year, month);
    }
}
