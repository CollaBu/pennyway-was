package kr.co.pennyway.domain.domains.spending.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.repository.SpendingCustomCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class SpendingCustomCategoryService {
    private final SpendingCustomCategoryRepository spendingCustomCategoryRepository;

    @Transactional
    public SpendingCustomCategory save(SpendingCustomCategory spendingCustomCategory) {
        return spendingCustomCategoryRepository.save(spendingCustomCategory);
    }
}
