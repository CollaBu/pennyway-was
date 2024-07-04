package kr.co.pennyway.api.common.security.authorization;

import kr.co.pennyway.domain.domains.spending.service.SpendingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component("spendingManager")
@RequiredArgsConstructor
public class SpendingManager {
    private final SpendingService spendingService;

    /**
     * 사용자가 해당 상세 지출 내역에 대한 권한이 있는지 확인한다. <br>
     *
     * @return 권한이 있으면 true, 없으면 false
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, Long spendingId) {
        return spendingService.isExistsSpending(userId, spendingId);
    }

    @Transactional(readOnly = true)
    public boolean hasPermissions(Long userId, List<Long> spendingIds) {
        if (spendingService.countByUserIdAndIdIn(userId, spendingIds) != (long) spendingIds.size()) {
            return false;
        }

        return true;
    }
}

