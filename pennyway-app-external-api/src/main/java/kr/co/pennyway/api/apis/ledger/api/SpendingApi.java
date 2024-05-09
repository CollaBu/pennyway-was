package kr.co.pennyway.api.apis.ledger.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.ledger.dto.SpendingReq;
import kr.co.pennyway.api.apis.ledger.dto.SpendingSearchRes;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "지출 내역 API")
public interface SpendingApi {
    @Operation(summary = "지출 내역 추가", method = "POST", description = "사용자의 지출 내역을 추가하고 추가된 지출 내역을 반환합니다.")
    @ApiResponse(responseCode = "200", content = @Content(schemaProperties = @SchemaProperty(name = "spending", schema = @Schema(implementation = SpendingSearchRes.Individual.class))))
    ResponseEntity<?> postSpending(@RequestBody @Validated SpendingReq request, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "지출 내역 조회", method = "GET", description = "사용자의 해당 년/월 지출 내역을 조회하고 월/일별 지출 총합을 반환합니다.")
    @Parameters({
            @Parameter(name = "year", description = "년도", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "month", description = "월", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponse(responseCode = "200", content = @Content(schemaProperties = @SchemaProperty(name = "spendings", schema = @Schema(implementation = SpendingSearchRes.Month.class))))
    ResponseEntity<?> getSpendingListAtYearAndMonth(@RequestParam("year") int year, @RequestParam("date") int month, @AuthenticationPrincipal SecurityUserDetails user);
}
