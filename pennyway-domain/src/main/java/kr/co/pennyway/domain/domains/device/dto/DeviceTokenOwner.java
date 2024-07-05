package kr.co.pennyway.domain.domains.device.dto;

/**
 * 디바이스 토큰과 유저 아이디를 담은 DTO
 */
public record DeviceTokenOwner(
        String deviceToken,
        Long userId,
        String name
) {
}
