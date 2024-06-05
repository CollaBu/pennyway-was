package kr.co.pennyway.api.apis.users.service;

import kr.co.pennyway.domain.domains.oauth.service.OauthService;
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

    @Transactional
    public void execute(Long userId) {
        if (!userService.isExistUser(userId)) throw new UserErrorException(UserErrorCode.NOT_FOUND);

        // TODO: [2024-05-03] 하나라도 채팅방의 방장으로 참여하는 경우 삭제 불가능 처리
        
        oauthService.deleteOauthsByUserId(userId);
        userService.deleteUser(userId);
    }
}
