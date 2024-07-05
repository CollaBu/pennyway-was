package kr.co.pennyway.domain.domains.device.dto;

import java.util.List;

/**
 * 디바이스 토큰과 유저 아이디를 담은 DTO
 */
public record DeviceTokenOwner(
        Long userId,
        String name,
        List<String> deviceTokens
) {
}
