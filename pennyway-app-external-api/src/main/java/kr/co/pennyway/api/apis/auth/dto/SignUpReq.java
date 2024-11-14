package kr.co.pennyway.api.apis.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kr.co.pennyway.api.common.validator.Password;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import kr.co.pennyway.domain.domains.user.type.Role;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * 회원가입 요청 Dto
 * <br/>
 * 일반 회원가입 시엔 General, 소셜 회원가입 시엔 Oauth를 사용합니다.
 */
public class SignUpReq {
    public record Info(String username, String name, String password, String phone, String code, String deviceId) {
        public String password(PasswordEncoder passwordEncoder) {
            return passwordEncoder.encode(password);
        }

        public User toEntity(PasswordEncoder bCryptPasswordEncoder) {
            return User.builder()
                    .username(username)
                    .name(name)
                    .password(bCryptPasswordEncoder.encode(password))
                    .passwordUpdatedAt(LocalDateTime.now())
                    .phone(phone)
                    .role(Role.USER)
                    .profileVisibility(ProfileVisibility.PUBLIC)
                    .notifySetting(NotifySetting.of(true, true, true))
                    .build();
        }

        @Override
        public String password() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public record OauthInfo(String oauthId, String idToken, String nonce, String name, String username, String phone,
                            String code, String deviceId) {
        public User toUser() {
            return User.builder()
                    .username(username)
                    .name(name)
                    .phone(phone)
                    .role(Role.USER)
                    .profileVisibility(ProfileVisibility.PUBLIC)
                    .notifySetting(NotifySetting.of(true, true, true))
                    .build();
        }
    }

    @Schema(name = "signUpReqGeneral", title = "일반 회원가입 요청 DTO")
    public record General(
            @Schema(description = "아이디", example = "pennyway")
            @NotBlank(message = "아이디를 입력해주세요")
            @Pattern(regexp = "^[a-z0-9-_.]{5,20}$", message = "영문 소문자, 숫자, 특수기호 (-), (_), (.) 만 사용하여, 5~20자의 아이디를 입력해 주세요")
            String username,
            @Schema(description = "이름", example = "페니웨이")
            @NotBlank(message = "이름을 입력해주세요")
            @Pattern(regexp = "^[가-힣a-zA-Z]{2,8}$", message = "한글과 영문 대, 소문자만 가능해요")
            String name,
            @Schema(description = "비밀번호", example = "pennyway1234")
            @NotBlank(message = "비밀번호를 입력해주세요")
            @Password(message = "8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해주세요. (적어도 하나의 영문 소문자, 숫자 포함)")
            String password,
            @Schema(description = "전화번호", example = "010-1234-5678")
            @NotBlank(message = "전화번호를 입력해주세요")
            @Pattern(regexp = "^01[01]-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
            String phone,
            @Schema(description = "6자리 정수 인증번호", example = "123456")
            @NotBlank(message = "인증번호는 필수입니다.")
            @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
            String code,
            @Schema(description = "사용자 기기 고유 식별자", example = "AA-BBB-CCC")
            @NotBlank(message = "사용자 기기 고유 식별자를 입력해주세요")
            String deviceId
    ) {
        public Info toInfo() {
            return new Info(username, name, password, phone, code, deviceId);
        }
    }

    @Schema(title = "일반 회원가입(소셜 계정 존재) 요청 DTO")
    public record SyncWithOauth(
            @Schema(description = "비밀번호", example = "pennyway1234")
            @NotBlank(message = "비밀번호를 입력해주세요")
            @Password(message = "8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해주세요. (적어도 하나의 영문 소문자, 숫자 포함)")
            String password,
            @Schema(description = "전화번호", example = "010-1234-5678")
            @NotBlank(message = "전화번호를 입력해주세요")
            @Pattern(regexp = "^01[01]-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
            String phone,
            @Schema(description = "6자리 정수 인증번호", example = "123456")
            @NotBlank(message = "인증번호는 필수입니다.")
            @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
            String code
    ) {
        public Info toInfo() {
            return new Info(null, null, password, phone, code, null);
        }
    }

    @Schema(title = "소셜 회원가입 요청 DTO")
    public record Oauth(
            @Schema(description = "OAuth id")
            @NotBlank(message = "OAuth id는 필수 입력값입니다.")
            String oauthId,
            @Schema(description = "OIDC 토큰")
            @NotBlank(message = "OIDC 토큰은 필수 입력값입니다.")
            String idToken,
            @Schema(description = "OIDC nonce")
            @NotBlank(message = "OIDC nonce는 필수 입력값입니다.")
            String nonce,
            @Schema(description = "아이디", example = "pennyway")
            @NotBlank(message = "아이디를 입력해주세요")
            @Pattern(regexp = "^[a-z0-9-_.]{5,20}$", message = "영문 소문자, 숫자, 특수기호 (-), (_), (.) 만 사용하여, 5~20자의 아이디를 입력해 주세요")
            String username,
            @Schema(description = "이름", example = "페니웨이")
            @NotBlank(message = "이름을 입력해주세요")
            @Pattern(regexp = "^[가-힣a-zA-Z]{2,8}$", message = "한글과 영문 대, 소문자만 가능해요")
            String name,
            @Schema(description = "전화번호", example = "010-1234-5678")
            @NotBlank(message = "전화번호를 입력해주세요")
            @Pattern(regexp = "^01[01]-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
            String phone,
            @Schema(description = "6자리 정수 인증번호", example = "123456")
            @NotBlank(message = "인증번호는 필수입니다.")
            @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
            String code,
            @Schema(description = "사용자 기기 고유 식별자", example = "AA-BBB-CCC")
            @NotBlank(message = "사용자 기기 고유 식별자를 입력해주세요")
            String deviceId
    ) {
        public OauthInfo toOauthInfo() {
            return new OauthInfo(oauthId, idToken, nonce, name, username, phone, code, deviceId);
        }
    }

    @Schema(title = "소셜 회원가입(기존 계정 존재) 요청 DTO")
    public record SyncWithAuth(
            @Schema(description = "OAuth id")
            @NotBlank(message = "OAuth id는 필수 입력값입니다.")
            String oauthId,
            @Schema(description = "OIDC 토큰")
            @NotBlank(message = "OIDC 토큰은 필수 입력값입니다.")
            String idToken,
            @Schema(description = "OIDC nonce")
            @NotBlank(message = "OIDC nonce는 필수 입력값입니다.")
            String nonce,
            @Schema(description = "전화번호", example = "010-1234-5678")
            @NotBlank(message = "전화번호를 입력해주세요")
            @Pattern(regexp = "^01[01]-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
            String phone,
            @Schema(description = "6자리 정수 인증번호", example = "123456")
            @NotBlank(message = "인증번호는 필수입니다.")
            @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
            String code,
            @Schema(description = "사용자 기기 고유 식별자", example = "AA-BBB-CCC")
            @NotBlank(message = "사용자 기기 고유 식별자를 입력해주세요")
            String deviceId
    ) {
        public OauthInfo toOauthInfo() {
            return new OauthInfo(oauthId, idToken, nonce, null, null, phone, code, deviceId);
        }
    }
}
