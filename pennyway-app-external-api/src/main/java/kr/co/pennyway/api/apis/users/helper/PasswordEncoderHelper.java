package kr.co.pennyway.api.apis.users.helper;

import kr.co.pennyway.common.annotation.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 암호화 도우미 클래스
 *
 * @author YANG JAESEO
 */
@Helper
@RequiredArgsConstructor
public class PasswordEncoderHelper {
    private final PasswordEncoder passwordEncoder;

    /**
     * 비밀번호 암호화 메서드
     *
     * @return password를 암호화한 문자열을 반환한다.
     */
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 비밀번호 일치 여부 확인 메서드
     *
     * @param actual   PasswordEncoder를 통해 암호화된 비밀번호
     * @param expected 비교할 비밀번호
     * @return 비밀번호가 일치하면 true, 일치하지 않으면 false를 반환한다.
     */
    public boolean isSamePassword(String actual, String expected) {
        return passwordEncoder.matches(actual, expected);
    }

    /**
     * 암호화된 비밀번호인지 확인하는 메서드. 암호화된 비밀번호는 $2a$로 시작한다.
     *
     * @return 암호화된 비밀번호이면 true, 아니면 false를 반환한다.
     */
    public boolean isEncodedPassword(String password) {
        return password.startsWith("$2a$");
    }
}
