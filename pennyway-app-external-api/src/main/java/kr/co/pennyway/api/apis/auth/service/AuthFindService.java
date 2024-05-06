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
        User user = readGeneralSignUpUser(phone);

        return AuthFindDto.FindUsernameRes.of(user);
    }

    /**
     * 일반 회원 비밀번호 찾기에 사용되는 코드 인증
     * 전화 번호를 이용해 사용자 조회후 사용자가 Oauth 사용자인지, General사용자인지 확인한다.
     *
     * @param phone 전화번호 (e.g. 010-1234-5678)
     */
    @Transactional(readOnly = true)
    public void existsGeneralSignUpUser(String phone) {
        readGeneralSignUpUser(phone);
    }

    /**
     * 일반 회원 비밀번호 찾기 & 변경하기
     *
     * @param phone       전화번호 (e.g. 010-1234-5678)
     * @param newPassword 새롭게 변경할 비밀번호 (e.g. qwer1234)
     */
    @Transactional
    public void updatePassword(String phone, String newPassword) {
        User user = readUserOrThrow(phone);

        user.updatePassword(passwordEncoderHelper.encodePassword(newPassword));
    }

    private User readGeneralSignUpUser(String phone) {
        User user = readUserOrThrow(phone);
        validateGeneralSignedUpUser(user);

        return user;
    }

    private User readUserOrThrow(String phone) {
        return userService.readUserByPhone(phone).orElseThrow(
                () -> {
                    log.info("해당 번호의 사용자를 찾을 수 없습니다: {}", phone);
                    throw new UserErrorException(UserErrorCode.NOT_FOUND);
                }
        );
    }

    private void validateGeneralSignedUpUser(User user) {
        if (!user.isGeneralSignedUpUser()) {
            log.info("일반 회원가입 이력이 없습니다.");
            throw new UserErrorException(UserErrorCode.DO_NOT_GENERAL_SIGNED_UP);
        }
    }
}