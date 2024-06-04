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

    /**
     * 사용자가 해당 TargetAmount에 대한 권한이 있는지 확인한다.
     *
     * @param userId         사용자 ID
     * @param targetAmountId TargetAmount ID
     * @return 권한 여부
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, Long targetAmountId) {
        return targetAmountService.isExistsTargetAmountByIdAndUserId(targetAmountId, userId);
    }
}
