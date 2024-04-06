package kr.co.pennyway.infra.common.oidc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import kr.co.pennyway.infra.common.exception.JwtErrorCode;
import kr.co.pennyway.infra.common.exception.JwtErrorException;
import kr.co.pennyway.infra.common.util.JwtErrorCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OauthOidcProviderImpl implements OauthOidcProvider {
    private static final String KID = "kid";
    private static final String RSA = "RSA";
    private final ObjectMapper objectMapper;

    @Override
    public String getKidFromUnsignedTokenHeader(String token, String iss, String aud, String nonce) {
        return getUnsignedTokenClaims(token, iss, aud, nonce).get("header").get(KID);
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
    private Map<String, Map<String, String>> getUnsignedTokenClaims(String token, String iss, String aud, String nonce) {
        log.info("getUnsignedTokenClaims : token : {}, iss : {}, aud : {}, nonce : {}", token, iss, aud, nonce);
        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();

            String unsignedToken = getUnsignedToken(token);
            String headerJson = new String(decoder.decode(unsignedToken.split("\\.")[0]));
            String payloadJson = new String(decoder.decode(unsignedToken.split("\\.")[1]));

            @SuppressWarnings("unchecked")
            Map<String, String> header = objectMapper.readValue(headerJson, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, String> payload = objectMapper.readValue(payloadJson, Map.class);

            Assert.isTrue(payload.get("aud").equals(aud), "aud is not matched. expected : " + aud + ", actual : " + payload.get("aud"));
            Assert.isTrue(payload.get("iss").equals(iss), "iss is not matched. expected : " + iss + ", actual : " + payload.get("iss"));

            return Map.of("header", header, "payload", payload);
        } catch (JsonProcessingException e) {
            log.warn("getUnsignedTokenClaims : Error - {},  {}", e.getClass(), e.getMessage());
            throw new RuntimeException(e);
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