package kr.co.pennyway.api.apis.auth.mapper;

import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.oauth.service.OauthService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 일반 회원가입, Oauth 회원가입 시나리오를 제어하여 유저 정보를 동기화하는 클래스
 *
 * @author YANG JAESEO
 */
@Slf4j
@Mapper
@RequiredArgsConstructor
public class UserSyncMapper {
    private final UserService userService;
    private final OauthService oauthService;

    /**
     * 일반 회원가입이 가능한 유저인지 확인
     *
     * @param phone String : 전화번호
     * @return Pair<Boolean, String> : 이미 가입된 회원인지 여부 (TRUE: 가입되지 않은 회원, FALSE: 가입된 회원), 가입된 회원인 경우 회원
     * ID 반환. 단, 이미 일반 회원가입을 한 유저인 경우에는 null을 반환한다.
     */
    @Transactional(readOnly = true)
    public Pair<Boolean, String> isGeneralSignUpAllowed(String phone) {
        Optional<User> user = userService.readUserByPhone(phone);

        if (user.isEmpty()) {
            log.info("회원가입 이력이 없는 사용자입니다. phone: {}", phone);
            return Pair.of(Boolean.FALSE, null);
        }

        if (user.get().getPassword() != null) {
            log.warn("이미 회원가입된 사용자입니다. phone: {}", phone);
            return null;
        }

        return Pair.of(Boolean.TRUE, user.get().getUsername());
    }

    @Transactional
    public
}
