package kr.co.pennyway.api.apis.ledge.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "목표금액 API")
public interface TargetAmountApi {
    @Operation(summary = "당월 목표 금액 등록/수정")
    @ApiResponse(responseCode = "400", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "목표 금액 등록 실패", value = """
                    {
                        "code": "4004",
                        "message": "당월 목표 금액에 대한 요청이 아닙니다."
                    }
                    """)
    }))
    ResponseEntity<?> putTargetAmount(@AuthenticationPrincipal SecurityUserDetails user);
}
