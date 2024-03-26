package kr.co.pennyway.api.apis.auth.helper;

import kr.co.pennyway.common.annotation.Helper;
import kr.co.pennyway.common.exception.GlobalErrorException;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Helper
@RequiredArgsConstructor
public class UserSyncHelper {
    private final UserService userService;

    /**
     * 일반 회원가입 시 이미 가입된 회원인지 확인
     *
     * @param phone String : 전화번호
     * @return Pair<Boolean, String> : 이미 가입된 회원인지 여부 (TRUE: 가입되지 않은 회원, FALSE: 가입된 회원), 가입된 회원인 경우 회원 ID 반환
     * @throws UserErrorException : 이미 일반 회원가입을 한 유저인 경우
     */
    @Transactional(readOnly = true)
    public Pair<Boolean, String> isSignedUserWhenGeneral(String phone) {
        User user;
        try {
            user = userService.readUserByPhone(phone);
        } catch (GlobalErrorException e) {
            log.info("User not found. phone: {}", phone);
            return Pair.of(Boolean.FALSE, null);
        }

        if (user.getPassword() != null) {
            log.warn("User already exists. phone: {}", phone);
            throw new UserErrorException(UserErrorCode.ALREADY_SIGNUP);
        }

        return Pair.of(Boolean.TRUE, user.getUsername());
    }
}
