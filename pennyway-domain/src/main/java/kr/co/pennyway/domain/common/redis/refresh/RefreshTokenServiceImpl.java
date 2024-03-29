package kr.co.pennyway.domain.common.redis.refresh;

import kr.co.pennyway.common.annotation.DomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Override
    public void save(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
        log.debug("리프레시 토큰 저장 : {}", refreshToken);
    }

    @Override
    public RefreshToken refresh(Long userId, String oldRefreshToken, String newRefreshToken) throws IllegalArgumentException, IllegalStateException {
        RefreshToken refreshToken = findOrElseThrow(userId);

        validateToken(oldRefreshToken, refreshToken);

        refreshToken.rotation(newRefreshToken);
        refreshTokenRepository.save(refreshToken);

        log.info("사용자 {}의 리프레시 토큰 갱신", userId);
        return refreshToken;
    }

    @Override
    public void delete(Long userId, String refreshToken) throws IllegalArgumentException {
        RefreshToken token = findOrElseThrow(userId);
        refreshTokenRepository.delete(token);
        log.info("사용자 {}의 리프레시 토큰 삭제", userId);
    }

    private RefreshToken findOrElseThrow(Long userId) {
        return refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("refresh token not found"));
    }

    /**
     * @param requestRefreshToken  String : 사용자가 보낸 refresh token
     * @param expectedRefreshToken String : Redis에 저장된 refresh token
     * @throws IllegalStateException : 요청한 토큰과 저장된 토큰이 다르다면 토큰이 탈취되었다고 판단하여 값 삭제
     */
    private void validateToken(String requestRefreshToken, RefreshToken expectedRefreshToken) throws IllegalStateException {
        if (isTakenAway(requestRefreshToken, expectedRefreshToken.getToken())) {
            log.warn("리프레시 토큰 불일치(탈취). expected : {}, actual : {}", requestRefreshToken, expectedRefreshToken.getToken());
            refreshTokenRepository.delete(expectedRefreshToken);
            log.info("사용자 {}의 리프레시 토큰 삭제", expectedRefreshToken.getUserId());

            throw new IllegalStateException("refresh token mismatched");
        }
    }

    /**
     * 토큰 탈취 여부 확인
     *
     * @param requestRefreshToken  String : 사용자가 보낸 refresh token
     * @param expectedRefreshToken String : Redis에 저장된 refresh token
     * @return boolean : 탈취되었다면 true, 아니면 false
     */
    private boolean isTakenAway(String requestRefreshToken, String expectedRefreshToken) {
        return !requestRefreshToken.equals(expectedRefreshToken);
    }
}
