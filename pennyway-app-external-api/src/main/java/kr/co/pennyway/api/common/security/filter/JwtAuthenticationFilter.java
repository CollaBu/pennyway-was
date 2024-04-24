package kr.co.pennyway.api.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaimKeys;
import kr.co.pennyway.domain.common.redis.forbidden.ForbiddenTokenService;
import kr.co.pennyway.infra.common.exception.JwtErrorCode;
import kr.co.pennyway.infra.common.exception.JwtErrorException;
import kr.co.pennyway.infra.common.jwt.JwtClaims;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터 <br/>
 * 만약, 유효한 액세스 토큰과 리프레시 토큰이 모두 없다면 익명 사용자로 간주한다. <br/>
 * 인증된 유저는 SecurityContextHolder에 SecurityUser를 등록하며, Controller에서 @AuthenticationPrincipal 어노테이션을 통해 접근할 수 있다.
 *
 * <pre>
 * {@code
 *  @GetMapping("/user")
 *  public ResponseEntity<User> getUser(@AuthenticationPrincipal SecurityUser user) {
 *      Long id = user.getId();
 *      ...
 *  }
 * }
 * </pre>
 *
 * @see org.springframework.security.core.annotation.AuthenticationPrincipal
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailService;
    private final ForbiddenTokenService forbiddenTokenService;

    private final JwtProvider accessTokenProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (isAnonymousRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = resolveAccessToken(request, response);

        UserDetails userDetails = getUserDetails(accessToken);
        authenticateUser(userDetails, request);
        filterChain.doFilter(request, response);
    }

    /**
     * AccessToken과 RefreshToken이 모두 없는 경우, 익명 사용자로 간주한다.
     */
    private boolean isAnonymousRequest(HttpServletRequest request) {
        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        return accessToken == null;
    }

    /**
     * @throws ServletException : Authorization 헤더가 없거나, 금지된 토큰이거나, 토큰이 만료된 경우 예외 발생
     */
    private String resolveAccessToken(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        String token = accessTokenProvider.resolveToken(authHeader);

        if (!StringUtils.hasText(token)) {
            handleAuthException(JwtErrorCode.EMPTY_ACCESS_TOKEN);
        }

        if (forbiddenTokenService.isForbidden(token)) {
            handleAuthException(JwtErrorCode.FORBIDDEN_ACCESS_TOKEN);
        }

        if (accessTokenProvider.isTokenExpired(token)) {
            handleAuthException(JwtErrorCode.EXPIRED_TOKEN);
        }

        return token;
    }

    /**
     * UserDetailsService를 통해 SecurityUser를 가져오는 메서드
     */
    private UserDetails getUserDetails(String accessToken) {
        JwtClaims claims = accessTokenProvider.getJwtClaimsFromToken(accessToken);
        String userId = (String) claims.getClaims().get(AccessTokenClaimKeys.USER_ID.getValue());

        return userDetailService.loadUserByUsername(userId);
    }

    /**
     * SecurityContextHolder에 SecurityUser를 등록하는 메서드
     */
    private void authenticateUser(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("Authenticated user: {}", userDetails.getUsername());
    }

    /**
     * 인증 예외가 발생했을 때, 로그를 남기고 예외를 던지는 메서드
     */
    private void handleAuthException(JwtErrorCode errorCode) throws ServletException {
        log.warn("AuthErrorException(code={}, message={})", errorCode.name(), errorCode.getExplainError());
        JwtErrorException exception = new JwtErrorException(errorCode);
        throw new ServletException(exception);
    }
}
