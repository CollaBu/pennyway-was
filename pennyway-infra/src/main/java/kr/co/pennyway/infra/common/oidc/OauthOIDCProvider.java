package kr.co.pennyway.infra.common.oidc;

public interface OauthOIDCProvider {
    /**
     * ID Token의 header에서 kid를 추출하는 메서드
     *
     * @param token : idToken
     * @param iss   : ID Token을 발급한 OAuth 2.0 제공자의 URL
     * @param aud   : ID Token이 발급된 앱의 앱 키
     * @param nonce : 인증 서버 로그인 요청 시 전달한 임의의 문자열
     * @return kid : ID Token의 서명에 사용된 공개키의 ID
     */
    String getKidFromUnsignedTokenHeader(String token, String iss, String aud, String nonce);

    /**
     * ID Token의 payload를 추출하는 메서드
     *
     * @param token    : idToken
     * @param modulus  : 공개키 모듈(n)
     * @param exponent : 공개키 지수(e)
     * @return OIDCDecodePayload : ID Token의 payload
     */
    OIDCDecodePayload getOIDCTokenBody(String token, String modulus, String exponent);
}
