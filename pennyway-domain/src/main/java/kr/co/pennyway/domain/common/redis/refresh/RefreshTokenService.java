package kr.co.pennyway.domain.common.redis.refresh;

public interface RefreshTokenService {
    /**
     * refresh token을 redis에 저장한다.
     *
     * @param refreshToken : {@link RefreshToken}
     */
    void save(RefreshToken refreshToken);

    /**
     * 사용자가 보낸 refresh token으로 기존 refresh token과 비교 검증 후, 새로운 refresh token으로 저장한다.
     *
     * @param userId          : 토큰 주인 pk
     * @param oldRefreshToken : 사용자가 보낸 refresh token
     * @param newRefreshToken : 교체할 refresh token
     * @return {@link RefreshToken}
     * @throws IllegalArgumentException : userId에 해당하는 refresh token이 없을 경우
     * @throws IllegalStateException    : 요청한 토큰과 저장된 토큰이 다르다면 토큰이 탈취되었다고 판단하여 값 삭제
     */
    RefreshToken refresh(Long userId, String oldRefreshToken, String newRefreshToken) throws IllegalArgumentException, IllegalStateException;

    /**
     * access token 으로 refresh token을 찾아서 제거 (로그아웃)
     *
     * @param userId       : 토큰 주인 pk
     * @param refreshToken : 검증용 refresh token
     * @throws IllegalArgumentException : userId에 해당하는 refresh token이 없을 경우
     */
    void delete(Long userId, String refreshToken) throws IllegalArgumentException;
}
