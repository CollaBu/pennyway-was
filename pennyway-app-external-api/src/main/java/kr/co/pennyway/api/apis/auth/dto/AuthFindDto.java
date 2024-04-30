package kr.co.pennyway.api.apis.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.pennyway.api.common.validator.Password;
import kr.co.pennyway.domain.domains.user.domain.User;

public class AuthFindDto {
    @Schema(title = "사용자 이름 찾기 응답 DTO", description = "전화번호로 사용자 이름 찾기 응답을 위한 DTO")
    public record FindUsernameRes(
            @Schema(description = "사용자 이름")
            String username
    ) {
        /**
         * 사용자 이름 찾기 응답 객체 생성
         *
         * @param username String : 사용자 이름
         */
        public static FindUsernameRes of(String username) {
            return new FindUsernameRes(username);
        }

        public static FindUsernameRes of(User user) {
            return new FindUsernameRes(user.getUsername());
        }
    }

    @Schema(title = "비밀번호 변경 요청 DTO", description = "전화번호로 사용자 비밀번호 변경을 위한 DTO")
    public record PasswordReq(
            @Schema(description = "새 비밀번호. 8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해주세요. (적어도 하나의 영문 소문자, 숫자 포함)", example = "newPassword")
            @NotBlank(message = "새 비밀번호를 입력해주세요")
            @Password(message = "8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해주세요. (적어도 하나의 영문 소문자, 숫자 포함)")
            String newPassword
    ) {
    }
}
