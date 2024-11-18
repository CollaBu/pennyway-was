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
     * @param deviceId        : 토큰 발급한 디바이스
     * @param oldRefreshToken : 사용자가 보낸 refresh token
     * @param newRefreshToken : 교체할 refresh token
     * @return {@link RefreshToken}
     * @throws IllegalArgumentException : userId에 해당하는 refresh token이 없을 경우
     * @throws IllegalStateException    : 요청한 토큰과 저장된 토큰이 다르다면 토큰이 탈취되었다고 판단하여 값 삭제
     */
    RefreshToken refresh(Long userId, String deviceId, String oldRefreshToken, String newRefreshToken) throws IllegalArgumentException, IllegalStateException;

    /**
     * 사용자에게 할당된 모든 Device의 refresh token을 삭제한다.
     *
     * @param userId : 토큰 주인 pk
     */
    void deleteAll(Long userId);
}
