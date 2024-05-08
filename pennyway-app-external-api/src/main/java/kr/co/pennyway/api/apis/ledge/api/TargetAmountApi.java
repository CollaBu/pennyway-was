package kr.co.pennyway.api.apis.ledge.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

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
    ResponseEntity<?> putTargetAmount(
            @RequestParam("date") @JsonSerialize(using = LocalDateSerializer.class) @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam("amount") @Min(value = 0, message = "amount 값은 0 이상이어야 합니다.") Integer amount,
            @AuthenticationPrincipal SecurityUserDetails user
    );
}
