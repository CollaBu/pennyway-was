package kr.co.pennyway.domain.domains.spending.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.spending.domain.SpendingCustomCategory;
import kr.co.pennyway.domain.domains.spending.repository.SpendingCustomCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class SpendingCustomCategoryRdbService {
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
    public List<SpendingCustomCategory> readSpendingCustomCategories(Long userId) {
        return spendingCustomCategoryRepository.findAllByUser_Id(userId);
    }

    @Transactional(readOnly = true)
    public boolean isExistsSpendingCustomCategory(Long userId, Long categoryId) {
        return spendingCustomCategoryRepository.existsByIdAndUser_Id(categoryId, userId);
    }

    @Transactional
    public void deleteSpendingCustomCategory(Long categoryId) {
        spendingCustomCategoryRepository.deleteById(categoryId);
    }

    @Transactional
    public void deleteSpendingCustomCategoriesByUserIdInQuery(Long userId) {
        spendingCustomCategoryRepository.deleteAllByUserIdInQuery(userId);
    }
}
