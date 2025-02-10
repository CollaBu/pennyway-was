package kr.co.pennyway.api.apis.users.service;

import kr.co.pennyway.domain.context.account.service.DeviceTokenService;
import kr.co.pennyway.domain.context.account.service.OauthService;
import kr.co.pennyway.domain.context.account.service.UserService;
import kr.co.pennyway.domain.context.chat.service.ChatMemberService;
import kr.co.pennyway.domain.context.finance.service.SpendingCategoryService;
import kr.co.pennyway.domain.context.finance.service.SpendingService;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
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

    private final ChatMemberService chatMemberService;

    private final SpendingService spendingService;
    private final SpendingCategoryService spendingCategoryService;

    /**
     * 사용자와 관련한 모든 데이터를 삭제(soft delete)하는 메서드
     * <p>
     * hard delete가 수행되어야 할 데이터는 삭제하지 않으며, 사용자 데이터 유지 기간이 만료될 때 DBA가 수행한다.
     *
     * @param userId
     */
    @Transactional
    public void execute(Long userId) {
        if (!userService.isExistUser(userId)) throw new UserErrorException(UserErrorCode.NOT_FOUND);

        if (chatMemberService.hasUserChatRoomOwnership(userId)) {
            throw new UserErrorException(UserErrorCode.HAS_OWNERSHIP_CHAT_ROOM);
        }

        oauthService.deleteOauth(userId);
        deviceTokenService.deleteDeviceTokensByUserId(userId);

        spendingService.deleteSpendingsByUserId(userId);
        spendingCategoryService.deleteSpendingCustomCategoriesByUserId(userId);

        userService.deleteUser(userId);
    }
}
