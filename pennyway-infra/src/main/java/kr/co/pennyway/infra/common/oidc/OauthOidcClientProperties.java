package kr.co.pennyway.infra.common.oidc;

public interface OauthOidcClientProperties {
    String getJwksUri();

    String getSecret();

    String getIssuer();
}
