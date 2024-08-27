package kr.co.pennyway.api.apis.users.service;

import kr.co.pennyway.domain.domains.device.service.DeviceTokenService;
import kr.co.pennyway.domain.domains.oauth.service.OauthService;
import kr.co.pennyway.domain.domains.spending.service.SpendingCustomCategoryService;
import kr.co.pennyway.domain.domains.spending.service.SpendingService;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 삭제만을 담당하는 클래스 <br/>
 * 추후 연관 관계의 데이터가 늘어나면 Template Method Pattern을 적용하여 단위 테스트를 수행할 수 있도록 한다.
 *
 * @author YANG JAESEO
 * @since 2024.05.03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDeleteService {
    private final UserService userService;
    private final OauthService oauthService;
    private final DeviceTokenService deviceTokenService;

    private final SpendingService spendingService;
    private final SpendingCustomCategoryService spendingCustomCategoryService;

    /**
     * 사용자와 관련한 모든 데이터를 삭제(soft delete)하는 메서드
     * <p>
     * hard delete가 수행되어야 할 데이터는 삭제하지 않으며, 사용자 데이터 유지 기간이 만료될 때 DBA가 수행한다.
     *
     * @param userId
     * @todo [2024-05-03] 채팅 기능이 추가되는 경우 채팅방장 탈퇴를 제한해야 하며, 추가로 삭제될 엔티티 삭제 로직을 추가해야 한다.
     */
    @Transactional
    public void execute(Long userId) {
        if (!userService.isExistUser(userId)) throw new UserErrorException(UserErrorCode.NOT_FOUND);

        // TODO: [2024-05-03] 하나라도 채팅방의 방장으로 참여하는 경우 삭제 불가능 처리

        oauthService.deleteOauthsByUserIdInQuery(userId);
        deviceTokenService.deleteDevicesByUserIdInQuery(userId);

        spendingService.deleteSpendingsByUserIdInQuery(userId);
        spendingCustomCategoryService.deleteSpendingCustomCategoriesByUserIdInQuery(userId);

        userService.deleteUser(userId);
    }
}
