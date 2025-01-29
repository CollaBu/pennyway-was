package kr.co.pennyway.api.apis.ledger.service;

import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.dto.CategoryInfo;
import kr.co.pennyway.domain.domains.spending.service.SpendingRdbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailySpendingAggregateService {
    private final SpendingRdbService spendingRdbService;

    @Transactional(readOnly = true)
    public List<Map.Entry<CategoryInfo, Long>> execute(Long userId, int year, int month, int day) {
        var spendings = spendingRdbService.readSpendings(userId, year, month, day);
        
        return spendings.stream()
                .collect(
                        groupingBy(
                                Spending::getCategory,
                                summingLong(Spending::getAmount)
                        )
                )
                .entrySet().stream()
                .sorted((o1, o2) -> (int) (o2.getValue() - o1.getValue()))
                .toList();
    }
}
