package kr.co.pennyway.api.apis.ledger.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;

@Tag(name = "목표금액 API")
public interface TargetAmountApi {
    @Operation(summary = "당월 목표 금액 등록/수정", method = "PUT")
    @ApiResponse(responseCode = "400", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "목표 금액 등록 실패", value = """
                    {
                        "code": "4004",
                        "message": "당월 목표 금액에 대한 요청이 아닙니다."
                    }
                    """)
    }))
    ResponseEntity<?> putTargetAmount(TargetAmountDto.UpdateParamReq request, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "당월 목표 금액 삭제", method = "DELETE")
    @ApiResponse(responseCode = "400", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "목표 금액 삭제 실패", value = """
                    {
                        "code": "4004",
                        "message": "당월 목표 금액에 대한 요청이 아닙니다."
                    }
                    """),
            @ExampleObject(name = "목표 금액 조회 실패", description = "목표 금액 데이터가 없거나, 이미 삭제(amount=-1)인 경우", value = """
                    {
                        "code": "4040",
                        "message": "해당 월의 목표 금액이 존재하지 않습니다."
                    }
                    """)
    }))
    ResponseEntity<?> deleteTargetAmount(@PathVariable LocalDate date, @AuthenticationPrincipal SecurityUserDetails user);
}
