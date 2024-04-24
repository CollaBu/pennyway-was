package kr.co.pennyway.api.apis.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
}
