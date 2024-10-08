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
    public String getKidFromUnsignedTokenHeader(String token, String iss, String sub, String aud, String nonce) {
        return getUnsignedTokenClaims(token, iss, sub, aud, nonce).get("header").get(KID);
    }

    @Override
    public OidcDecodePayload getOIDCTokenBody(String token, String modulus, String exponent) {
        Claims body = getOIDCTokenJws(token, modulus, exponent).getPayload();
        String aud = body.getAudience().iterator().next(); // aud가 여러개일 경우 첫 번째 aud를 사용

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
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> getUnsignedTokenClaims(String token, String iss, String sub, String aud, String nonce) {
        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();

            String unsignedToken = getUnsignedToken(token);
            String headerJson = new String(decoder.decode(unsignedToken.split("\\.")[0]));
            String payloadJson = new String(decoder.decode(unsignedToken.split("\\.")[1]));

            Map<String, String> header = objectMapper.readValue(headerJson, Map.class);
            Map<String, String> payload = objectMapper.readValue(payloadJson, Map.class);

            Assert.isTrue(payload.get("iss").equals(iss), "iss is not matched. expected : " + iss + ", actual : " + payload.get("iss"));
            Assert.isTrue(payload.get("sub").equals(sub), "sub is not matched. expected : " + sub + ", actual : " + payload.get("sub"));
            Assert.isTrue(payload.get("aud").equals(aud), "aud is not matched. expected : " + aud + ", actual : " + payload.get("aud"));
            Assert.isTrue(payload.get("nonce").equals(nonce), "nonce is not matched. expected : " + nonce + ", actual : " + payload.get("nonce"));

            return Map.of("header", header, "payload", payload);
        } catch (IllegalArgumentException e) {
            log.warn("getUnsignedTokenClaims : Error - {},  {}", e.getClass(), e.getMessage());
            throw new JwtErrorException(JwtErrorCode.FAILED_AUTHENTICATION);
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
        return splitToken[0] + "." + splitToken[1];
    }

    /**
     * 공개키로 서명을 검증하는 메서드
     */
    private Jws<Claims> getOIDCTokenJws(String token, String modulus, String exponent) {
        try {
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