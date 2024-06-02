package kr.co.pennyway.api.common.security.authorization;

import kr.co.pennyway.domain.domains.target.service.TargetAmountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component("targetAmountManager")
@RequiredArgsConstructor
public class TargetAmountManager {
    private final TargetAmountService targetAmountService;

    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, Long targetAmountId) {
        return targetAmountService.isExistsTargetAmountByIdAndUserId(userId, targetAmountId);
    }
}
