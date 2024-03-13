package kr.co.infra.common.jwt;

import java.util.Map;

public interface JwtClaims {
    Map<String, ?> getClaims();
}
