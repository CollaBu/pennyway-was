package kr.co.pennyway.api.apis.users.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.api.apis.users.dto.UserProfileDto;
import kr.co.pennyway.api.apis.users.dto.UserProfileUpdateDto;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "사용자 계정 관리 API", description = "사용자 본인의 계정 관리를 위한 Usecase를 제공합니다.")
public interface UserAccountApi {
    @Operation(summary = "디바이스 등록", description = "사용자의 디바이스 정보를 등록(originToken == newToken)하거나 갱신(originToken != newToken)합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "디바이스 등록 성공", value = """
                            {
                                "device": {
                                    "id": 1,
                                    "token": "newToken"
                                }
                            }
                            """)
            })),
            @ApiResponse(responseCode = "404", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "수정 요청 시, token에 매칭하는 디바이스 정보가 없는 경우", value = """
                            {
                                "code": "4040",
                                "message": "디바이스를 찾을 수 없습니다."
                            }
                            """)
            }))
    })
    ResponseEntity<?> putDevice(@RequestBody @Validated DeviceDto.RegisterReq request, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "디바이스 토큰 제거", description = "사용자의 디바이스 정보와 토큰을 제거합니다.")
    @Parameter(name = "token", description = "삭제할 디바이스 토큰", required = true, in = ParameterIn.QUERY)
    @ApiResponse(responseCode = "404", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "수정 요청 시, token에 매칭하는 디바이스 정보가 없는 경우", value = """
                    {
                        "code": "4040",
                        "message": "디바이스를 찾을 수 없습니다."
                    }
                    """)
    }))
    ResponseEntity<?> deleteDevice(@RequestParam("token") @Validated @NotBlank String token, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "사용자 계정 조회", description = "사용자 본인의 계정 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", content = @Content(schemaProperties = @SchemaProperty(name = "user", schema = @Schema(implementation = UserProfileDto.class))))
    ResponseEntity<?> getMyAccount(@AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "사용자 이름 수정")
    ResponseEntity<?> putName(@RequestBody @Validated UserProfileUpdateDto.NameReq request, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "사용자 아이디 수정")
    ResponseEntity<?> putUsername(@RequestBody @Validated UserProfileUpdateDto.UsernameReq request, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "사용자 비밀번호 검증")
    @ApiResponses({
            @ApiResponse(responseCode = "400", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "비밀번호 불일치", value = """
                            {
                                "code": "4004",
                                "message": "비밀번호가 일치하지 않습니다."
                            }
                            """)
            })),
            @ApiResponse(responseCode = "403", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "일반 회원가입 이력이 없는 경우", value = """
                            {
                                "code": "4030",
                                "message": "일반 회원가입 계정이 아닙니다."
                            }
                            """)
            }))
    })
    ResponseEntity<?> postPasswordVerification(@RequestBody @Validated UserProfileUpdateDto.PasswordVerificationReq request, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "사용자 알림 활성화")
    @Parameter(name = "type", description = "알림 타입", examples = {
            @ExampleObject(name = "가계부", value = "account_book"), @ExampleObject(name = "피드", value = "feed"), @ExampleObject(name = "채팅", value = "chat")
    }, required = true, in = ParameterIn.QUERY)
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "가계부 알림 활성화", value = """
                            {
                                "code": "2000",
                                "data": {
                                    "notifySetting": {
                                        "accountBookNotify": true
                                    }
                                }
                            }
                            """),
                    @ExampleObject(name = "피드 알림 활성화", value = """
                            {
                                "code": "2000",
                                "data": {
                                    "notifySetting": {
                                        "feedNotify": true
                                    }
                                }
                            }
                            """),
                    @ExampleObject(name = "채팅 알림 활성화", value = """
                            {
                                "code": "2000",
                                "data": {
                                    "notifySetting": {
                                        "chatNotify": true
                                    }
                                }
                            }
                            """)
            }))
    })
    ResponseEntity<?> patchNotifySetting(@RequestParam NotifySetting.NotifyType type, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "사용자 알림 비활성화")
    @Parameter(name = "type", description = "알림 타입", examples = {
            @ExampleObject(name = "가계부", value = "account_book"), @ExampleObject(name = "피드", value = "feed"), @ExampleObject(name = "채팅", value = "chat")
    }, required = true, in = ParameterIn.QUERY)
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "가계부 알림 비활성화", value = """
                            {
                                "code": "2000",
                                "data": {
                                    "notifySetting": {
                                        "accountBookNotify": false
                                    }
                                }
                            }
                            """),
                    @ExampleObject(name = "피드 알림 비활성화", value = """
                            {
                                "code": "2000",
                                "data": {
                                    "notifySetting": {
                                        "feedNotify": false
                                    }
                                }
                            }
                            """),
                    @ExampleObject(name = "채팅 알림 비활성화", value = """
                            {
                                "code": "2000",
                                "data": {
                                    "notifySetting": {
                                        "chatNotify": false
                                    }
                                }
                            }
                            """)
            }))
    })
    ResponseEntity<?> deleteNotifySetting(@RequestParam NotifySetting.NotifyType type, @AuthenticationPrincipal SecurityUserDetails user);
}
