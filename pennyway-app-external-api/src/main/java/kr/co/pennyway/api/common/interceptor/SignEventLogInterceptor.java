package kr.co.pennyway.api.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.pennyway.api.common.security.jwt.JwtClaimsParserUtil;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaimKeys;
import kr.co.pennyway.domain.common.redis.sign.SignEventLog;
import kr.co.pennyway.domain.common.redis.sign.SignEventLogService;
import kr.co.pennyway.domain.domains.sign.type.IpAddressHeader;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Slf4j
public class SignEventLogInterceptor implements HandlerInterceptor {
    /**
     * <p>
     * User-Agent에서 앱 버전을 추출하기 위한 정규식 패턴이다.
     * User-Agent는 "AppName/Version (platform; os; deviceModel)" 형식으로 되어있는 문자열이다.
     * </p>
     */
    private static final Pattern pattern = Pattern.compile("^(\\w+)/(\\d+\\.\\d+) \\((\\w+); (\\w+ \\d+\\.\\d+); (\\w+\\d+,\\d+)\\)$");
    private final SignEventLogService signEventLogService;
    private final JwtProvider accessTokenProvider;

    public SignEventLogInterceptor(SignEventLogService signEventLogService, JwtProvider accessTokenProvider) {
        this.signEventLogService = signEventLogService;
        this.accessTokenProvider = accessTokenProvider;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
        if (response.getStatus() != 200) {
            return;
        }

        String accessToken = response.getHeader(HttpHeaders.AUTHORIZATION);
        Long userId = JwtClaimsParserUtil.getClaimsValue(accessTokenProvider.getJwtClaimsFromToken(accessToken), AccessTokenClaimKeys.USER_ID.getValue(), Long::parseLong);

        UserAgentInfo userAgent = getUserAgentInfo(request.getHeader(HttpHeaders.USER_AGENT));
        Pair<IpAddressHeader, String> ipAddress = getClientIP(request);

        SignEventLog signEventLog = SignEventLog.builder()
                .userId(userId)
                .ipAddressHeader(ipAddress.getKey().getType())
                .ipAddress(ipAddress.getValue())
                .appVersion(userAgent.appVersion())
                .deviceModel(userAgent.deviceModel())
                .os(userAgent.os())
                .signedAt(LocalDateTime.now())
                .build();
        log.debug("SignEventLog: {}", signEventLog);

        signEventLogService.create(signEventLog);
    }

    private Pair<IpAddressHeader, String> getClientIP(HttpServletRequest request) {
        IpAddressHeader headerType = IpAddressHeader.X_FORWARDED_FOR;
        String ip = request.getHeader(headerType.getType());

        if (ip == null) {
            headerType = IpAddressHeader.PROXY_CLIENT_IP;
            ip = request.getHeader(headerType.getType());
        }
        if (ip == null) {
            headerType = IpAddressHeader.WL_PROXY_CLIENT_IP;
            ip = request.getHeader(headerType.getType());
        }
        if (ip == null) {
            headerType = IpAddressHeader.HTTP_CLIENT_IP;
            ip = request.getHeader(headerType.getType());
        }
        if (ip == null) {
            headerType = IpAddressHeader.HTTP_X_FORWARDED_FOR;
            ip = request.getHeader(headerType.getType());
        }
        if (ip == null) {
            headerType = IpAddressHeader.REMOTE_ADDR;
            ip = request.getRemoteAddr();
        }

        return Pair.of(headerType, ip);
    }

    private UserAgentInfo getUserAgentInfo(String userAgent) {
        var matcher = pattern.matcher(userAgent);
        if (!matcher.matches()) {
            return new UserAgentInfo("", "", "", "", "");
        }

        return new UserAgentInfo(
                matcher.group(1),
                matcher.group(2),
                matcher.group(3),
                matcher.group(5),
                matcher.group(4)
        );
    }

    private record UserAgentInfo(
            String appName,
            String appVersion,
            String platform,
            String deviceModel,
            String os
    ) {
    }
}
