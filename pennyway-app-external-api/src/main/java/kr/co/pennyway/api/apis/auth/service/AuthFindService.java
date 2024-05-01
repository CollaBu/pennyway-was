package kr.co.pennyway.api.apis.auth.service;

import kr.co.pennyway.api.apis.auth.dto.AuthFindDto;
import kr.co.pennyway.api.apis.users.helper.PasswordEncoderHelper;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthFindService {
    private final UserService userService;
    private final PasswordEncoderHelper passwordEncoderHelper;

    /**
     * 일반 회원 아이디 찾기
     *
     * @param phone 전화번호 (e.g. 010-1234-5678)
     * @return AuthFindDto.FindPasswordRes 비밀번호 찾기 응답
     */
    @Transactional(readOnly = true)
    public AuthFindDto.FindUsernameRes findUsername(String phone) {
        User user = readUserOrThrow(phone);
        checkUserPassword(user);

        return AuthFindDto.FindUsernameRes.of(user);
    }

    /**
     * 일반 회원 비밀번호 찾기 & 수정하기
     *
     * @param phone       전화번호 (e.g. 010-1234-5678)
     * @param newPassword 새롭게 변경할 비밀번호 (e.g. qwer1234)
     */
    @Transactional
    public void updatePassword(String phone, String newPassword) {
        User user = readUserOrThrow(phone);
        String password = checkUserPassword(user);

        if (passwordEncoderHelper.isSamePassword(user.getPassword(), newPassword)) {
            log.info("기존과 동일한 비밀번호로는 변경할 수 없습니다.");
            throw new UserErrorException(UserErrorCode.PASSWORD_NOT_CHANGED);
        }

        user.updatePassword(passwordEncoderHelper.encodePassword(newPassword));
    }

    // 유저 존재 여부 검증
    private User readUserOrThrow(String phone) {
        return userService.readUserByPhone(phone).orElseThrow(
                () -> {
                    log.info("해당 번호의 사용자를 찾을 수 없습니다: {}", phone);
                    throw new UserErrorException(UserErrorCode.NOT_FOUND);
                }
        );
    }

    // Oauth 회원 여부 검증
    private String checkUserPassword(User user) {
        String password = user.getPassword();
        if (password == null) {
            log.info("해당 번호의 사용자는 Oauth 유저 입니다.: {}", user.getPhone());
            throw new UserErrorException(UserErrorCode.NOT_FOUND);
        }

        return password;
    }
}
