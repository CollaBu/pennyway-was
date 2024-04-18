package kr.co.pennyway.api.apis.users.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "사용자 계정 관리 API", description = "사용자 본인의 계정 관리를 위한 Usecase를 제공합니다.")
@SecurityRequirement(name = "access-token")
public interface UserAccountApi {
    @Operation(summary = "디바이스 등록", description = "사용자의 디바이스 정보를 등록하거나 갱신합니다.")
    ResponseEntity<?> putDevice(@RequestBody @Validated DeviceDto.RegisterReq request, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "디바이스 토큰 제거", description = "사용자의 디바이스 정보와 토큰을 제거합니다.")
    @Parameter(name = "token", description = "삭제할 디바이스 토큰", required = true, in = ParameterIn.QUERY)
    ResponseEntity<?> deleteDevice(@RequestParam("token") @Validated @NotBlank String token, @AuthenticationPrincipal SecurityUserDetails user);
}
