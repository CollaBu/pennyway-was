package kr.co.pennyway.domain.domains.sign.type;

import kr.co.pennyway.domain.common.converter.LegacyCommonType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IpAddressHeader implements LegacyCommonType {
    X_FORWARDED_FOR("0", "X-Forwarded-For"),
    PROXY_CLIENT_IP("1", "Proxy-Client-IP"),
    WL_PROXY_CLIENT_IP("2", "WL-Proxy-Client-IP"),
    HTTP_CLIENT_IP("3", "HTTP_CLIENT_IP"),
    HTTP_X_FORWARDED_FOR("4", "HTTP_X_FORWARDED_FOR"),
    REMOTE_ADDR("5", "REMOTE_ADDR");

    private final String code;
    private final String type;
}
