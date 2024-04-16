package kr.co.pennyway.api.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {
    /**
     * request에서 cookieName에 해당하는 쿠키를 찾아서 반환합니다.
     *
     * @param request    HttpServletRequest : 쿠키를 찾을 request
     * @param cookieName String : 찾을 쿠키의 이름
     * @return Optional<Cookie> : 쿠키가 존재하면 해당 쿠키를, 존재하지 않으면 Optional.empty()를 반환합니다.
     */
    public Optional<Cookie> getCookieFromRequest(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .findAny();
    }

    /**
     * cookieName에 해당하는 쿠키를 생성합니다.
     *
     * @param cookieName String : 생성할 쿠키의 이름
     * @param value      String : 생성할 쿠키의 값
     * @param maxAge     long : 생성할 쿠키의 만료 시간
     * @return ResponseCookie : 생성된 쿠키
     */
    public ResponseCookie createCookie(String cookieName, String value, long maxAge) {
        return ResponseCookie.from(cookieName, value)
                .path("/")
                .httpOnly(true)
                .maxAge(maxAge)
                .secure(true)
                .sameSite("None")
                .build();
    }

    /**
     * cookieName에 해당하는 쿠키를 제거합니다.
     *
     * @return Optional<ResponseCookie> : 쿠키가 존재하면 제거된 쿠키를, 존재하지 않으면 Optional.empty()를 반환합니다.
     */
    public ResponseCookie deleteCookie(String cookieName) {
        return createCookie(cookieName, "", 0);
    }
}

