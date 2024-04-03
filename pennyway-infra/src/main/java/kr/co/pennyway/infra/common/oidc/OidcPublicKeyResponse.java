package kr.co.pennyway.infra.common.oidc;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OidcPublicKeyResponse {
    List<OidcPublicKey> keys;

    @Override
    public String toString() {
        return "OIDCPublicKeyResponse{" +
                "keys=" + keys +
                '}';
    }
}
