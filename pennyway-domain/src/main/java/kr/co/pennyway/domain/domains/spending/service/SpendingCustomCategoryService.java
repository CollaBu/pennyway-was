package kr.co.pennyway.domain.domains.spending.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.repository.SpendingCustomCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class SpendingCustomCategoryService {
    private final SpendingCustomCategoryRepository spendingCustomCategoryRepository;

    @Transactional
    public SpendingCustomCategory createSpendingCustomCategory(SpendingCustomCategory spendingCustomCategory) {
        return spendingCustomCategoryRepository.save(spendingCustomCategory);
    }

    @Transactional(readOnly = true)
    public Optional<SpendingCustomCategory> readSpendingCustomCategory(Long id) {
        return spendingCustomCategoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean isExistsSpendingCustomCategory(Long userId, Long categoryId) {
        return spendingCustomCategoryRepository.existsByIdAndUser_Id(categoryId, userId);
    }
}
