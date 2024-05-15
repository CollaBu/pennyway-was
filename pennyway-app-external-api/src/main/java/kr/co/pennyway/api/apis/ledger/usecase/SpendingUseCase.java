package kr.co.pennyway.api.apis.ledger.usecase;

import kr.co.pennyway.api.apis.ledger.dto.SpendingReq;
import kr.co.pennyway.api.apis.ledger.dto.SpendingSearchRes;
import kr.co.pennyway.api.apis.ledger.mapper.SpendingMapper;
import kr.co.pennyway.api.apis.ledger.service.SpendingSaveService;
import kr.co.pennyway.api.apis.ledger.service.SpendingSearchService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorCode;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorException;
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
    private final SpendingSaveService spendingSaveService;
    private final SpendingSearchService spendingSearchService;
    private final SpendingService spendingService;

    private final UserService userService;


    @Transactional
    public SpendingSearchRes.Individual createSpending(Long userId, SpendingReq request) {
        User user = userService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));

        Spending spending = spendingSaveService.createSpending(user, request);

        return SpendingMapper.toSpendingSearchResIndividual(spending);
    }

    @Transactional(readOnly = true)
    public SpendingSearchRes.Month getSpendingsAtYearAndMonth(Long userId, int year, int month) {
        List<Spending> spendings = spendingSearchService.readSpendings(userId, year, month);

        return SpendingMapper.toSpendingSearchResMonth(spendings, year, month);
    }

    @Transactional(readOnly = true)
    public SpendingSearchRes.Individual getSpedingDetail(Long userId, Long spendingId) {
        Spending spending = spendingService.readSpending(spendingId)
                .orElseThrow(() -> new SpendingErrorException(SpendingErrorCode.NOT_FOUND_SPENDING));

        return SpendingMapper.toSpendingSearchResIndividual(spending);
    }
}
