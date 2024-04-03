package kr.co.pennyway.infra.common.oidc;

public record OidcDecodePayload(
        /* issuer */
        String iss,
        /* client id */
        String aud,
        /* aouth provider account unique id */
        String sub,
        String email
) {
}
