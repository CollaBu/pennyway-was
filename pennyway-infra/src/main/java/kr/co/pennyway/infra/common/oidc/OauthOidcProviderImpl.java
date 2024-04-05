package kr.co.pennyway.infra.common.oidc;

import io.jsonwebtoken.*;
import kr.co.pennyway.infra.common.exception.JwtErrorCode;
import kr.co.pennyway.infra.common.exception.JwtErrorException;
import kr.co.pennyway.infra.common.util.JwtErrorCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class OauthOidcProviderImpl implements OauthOidcProvider {
    private static final String KID = "kid";
    private static final String RSA = "RSA";

    @Override
    public String getKidFromUnsignedTokenHeader(String token, String iss, String aud, String nonce) {
        return (String) getUnsignedTokenClaims(token, iss, aud, nonce).getHeader().get(KID);
    }

    @Override
    public OidcDecodePayload getOIDCTokenBody(String token, String modulus, String exponent) {
        Claims body = getOIDCTokenJws(token, modulus, exponent).getPayload();
        String aud = body.getAudience().iterator().next(); // TODO: 이전 버전과 다르게 aud가 Set<String>으로 변경되어 있음. 테스트 필요
        log.debug("aud : {}", aud);

        return new OidcDecodePayload(
                body.getIssuer(),
                aud,
                body.getSubject(),
                body.get("email", String.class));
    }

    /**
     * ID Token의 header와 body를 Base64 방식으로 디코딩하는 메서드 <br/>
     * payload의 iss, aud, exp, nonce를 검증하고, 실패시 예외 처리
     */
    private Jwt<Header, Claims> getUnsignedTokenClaims(String token, String iss, String aud, String nonce) {
        try {
            return Jwts.parser()
                    .requireAudience(aud)
                    .requireIssuer(iss)
//                    .require("nonce", nonce) // 현재는 nonce를 사용하지 않음
                    .build()
                    .parseUnsecuredClaims(getUnsignedToken(token)); // TODO: 기존 방식은 parseClaimsJwt(getUnsignedToken(token)); -> 변경한 코드 정상 동작 여부 확인 필요
        } catch (JwtException e) {
            final JwtErrorCode errorCode = JwtErrorCodeUtil.determineErrorCode(e, JwtErrorCode.FAILED_AUTHENTICATION);

            log.warn("getUnsignedTokenClaims : Error code : {}, Error - {},  {}", errorCode, e.getClass(), e.getMessage());
            throw new JwtErrorException(errorCode);
        }
    }

    /**
     * Token의 signature를 제거하는 메서드
     */
    private String getUnsignedToken(String token) {
        String[] splitToken = token.split("\\.");
        if (splitToken.length != 3) throw new JwtErrorException(JwtErrorCode.MALFORMED_TOKEN);
        return splitToken[0] + "." + splitToken[1] + ".";
    }

    /**
     * 공개키로 서명을 검증하는 메서드
     */
    private Jws<Claims> getOIDCTokenJws(String token, String modulus, String exponent) {
        try {
            log.info("token : {}", token);
            return Jwts.parser()
                    .verifyWith(getRSAPublicKey(modulus, exponent))
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException e) {
            final JwtErrorCode errorCode = JwtErrorCodeUtil.determineErrorCode(e, JwtErrorCode.FAILED_AUTHENTICATION);

            log.warn("getOIDCTokenJws : Error code : {}, Error - {},  {}", errorCode, e.getClass(), e.getMessage());
            throw new JwtErrorException(errorCode);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.warn("getOIDCTokenJws : Error - {},  {}", e.getClass(), e.getMessage());
            throw new JwtErrorException(JwtErrorCode.MALFORMED_TOKEN);
        }
    }

    /**
     * n, e 조합으로 공개키를 생성하는 메서드
     */
    private PublicKey getRSAPublicKey(String modulus, String exponent) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        byte[] decodeN = Base64.getUrlDecoder().decode(modulus);
        byte[] decodeE = Base64.getUrlDecoder().decode(exponent);
        BigInteger n = new BigInteger(1, decodeN);
        BigInteger e = new BigInteger(1, decodeE);

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
        return keyFactory.generatePublic(publicKeySpec);
    }
}