package kr.co.pennyway.infra.common.oidc;

public record OIDCPublicKey(
        String kid,
        String kty,
        String alg,
        String use,
        String n,
        String e
) {
}
