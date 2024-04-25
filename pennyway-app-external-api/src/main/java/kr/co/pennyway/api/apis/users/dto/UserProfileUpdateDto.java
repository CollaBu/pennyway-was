package kr.co.pennyway.api.apis.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserProfileUpdateDto {
    @Schema(title = "이름 변경 요청 DTO")
    public record NameReq(
            @Schema(description = "이름", example = "페니웨이")
            @NotBlank(message = "이름을 입력해주세요")
            @Pattern(regexp = "^[가-힣a-z]{2,8}$", message = "2~8자의 한글, 영문 소문자만 사용 가능합니다.")
            String name
    ) {
    }

    @Schema(title = "아이디 변경 요청 DTO")
    public record UsernameReq(
            @Schema(description = "아이디", example = "pennyway")
            @NotBlank(message = "아이디를 입력해주세요")
            @Pattern(regexp = "^[a-z-_.]{5,20}$", message = "5~20자의 영문 소문자, -, _, . 만 사용 가능합니다.")
            String username
    ) {
    }
}