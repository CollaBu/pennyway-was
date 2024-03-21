package kr.co.pennyway.api.apis.auth.dto;

public record Jwts(
        String accessToken,
        String refreshToken
) {
    public static Jwts of(String accessToken, String refreshToken) {
        return new Jwts(accessToken, refreshToken);
    }
}
