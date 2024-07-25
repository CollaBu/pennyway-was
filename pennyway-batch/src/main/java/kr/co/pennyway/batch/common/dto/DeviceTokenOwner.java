package kr.co.pennyway.batch.common.dto;

/**
 * 디바이스 토큰과 유저 아이디를 담은 DTO
 */
public record DeviceTokenOwner(
        Long userId,
        Long deviceTokenId,
        String name,
        String deviceToken
) {
}
