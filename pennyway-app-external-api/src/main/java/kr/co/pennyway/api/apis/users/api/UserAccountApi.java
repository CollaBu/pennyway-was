package kr.co.pennyway.api.apis.users.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "사용자 계정 관리 API", description = "사용자 본인의 계정 관리를 위한 Usecase를 제공합니다.")
@SecurityRequirement(name = "access-token")
public interface UserAccountApi {
    @Operation(summary = "디바이스 등록", description = "사용자의 디바이스 정보를 등록하거나 갱신합니다.")
    ResponseEntity<?> registerDevice(@RequestBody @Validated DeviceDto.RegisterReq request, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "디바이스 해제", description = "사용자의 디바이스 정보를 제거합니다.")
    ResponseEntity<?> unregisterDevice(@PathVariable Long deviceId, @AuthenticationPrincipal SecurityUserDetails user);
}
