package kr.co.pennyway.domain.domains.spending.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.spending.domain.Spending;
import kr.co.pennyway.domain.domains.spending.repository.SpendingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class SpendingService {
    private final SpendingRepository spendingRepository;

    @Transactional
    public Spending save(Spending spending) {
        return spendingRepository.save(spending);
    }
}
