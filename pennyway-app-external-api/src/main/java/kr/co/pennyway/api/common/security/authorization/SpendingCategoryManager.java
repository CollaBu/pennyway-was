package kr.co.pennyway.api.common.security.authorization;

import kr.co.pennyway.api.common.query.SpendingCategoryType;
import kr.co.pennyway.domain.domains.spending.service.SpendingCustomCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component("spendingCategoryManager")
@RequiredArgsConstructor
public class SpendingCategoryManager {
    private final SpendingCustomCategoryService spendingCustomCategoryService;

    /**
     * 사용자가 커스텀 지출 카테고리에 대한 권한이 있는지 확인한다. <br>
     * -1L이면 서비스에서 제공하는 기본 카테고리를 사용하는 것이므로 무시한다.
     *
     * @return 권한이 있으면 true, 없으면 false
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, Long categoryId) {
        if (categoryId.equals(-1L)) {
            return true;
        }

        return spendingCustomCategoryService.isExistsSpendingCustomCategory(userId, categoryId);
    }

    /**
     * 사용자가 지출 카테고리에 대한 권한이 있는지 확인한다.
     * {@link SpendingCategoryType#CUSTOM}이면 {@link #hasPermission(Long, Long)}를 호출한다.
     * {@link SpendingCategoryType#DEFAULT}면, 시스템 제공 카테고리이므로 권한 검사를 수행하지 않는다.
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, Long categoryId, SpendingCategoryType type) {
        if (type.equals(SpendingCategoryType.CUSTOM)) {
            return hasPermission(userId, categoryId);
        }

        return true;
    }
}
