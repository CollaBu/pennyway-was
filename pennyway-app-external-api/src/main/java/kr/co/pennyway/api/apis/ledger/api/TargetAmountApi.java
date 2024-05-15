package kr.co.pennyway.api.apis.ledger.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
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

    @Operation(summary = "임의의 년/월에 대한 목표 금액 및 총 사용 금액 조회", method = "GET", description = "일수는 무시하고 년/월 정보만 사용한다. 일반적으로 당월 정보 요청에 사용하는 API이다.")
    @Parameter(name = "date", description = "현재 날짜(yyyy-MM-dd)", required = true, example = "2024-05-08", in = ParameterIn.PATH)
    @ApiResponse(responseCode = "200", description = "목표 금액 및 총 사용 금액 조회 성공", content = @Content(
            schemaProperties = @SchemaProperty(name = "targetAmount", schema = @Schema(implementation = TargetAmountDto.WithTotalSpendingRes.class))))
    ResponseEntity<?> getTargetAmountAndTotalSpending(@PathVariable LocalDate date, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "사용자 가입 이후 현재까지의 목표 금액 및 총 사용 금액 리스트 조회", method = "GET", description = "일수는 무시하고 년/월 정보만 사용한다. 데이터가 존재하지 않을 때 더미 값을 사용하며, 최신 데이터 순으로 정렬된 응답을 반환한다.")
    @Parameters({
            @Parameter(name = "date", description = "현재 날짜(yyyy-MM-dd)", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "param", hidden = true)
    })
    @ApiResponse(responseCode = "200", description = "목표 금액 및 총 사용 금액 리스트 조회 성공", content = @Content(
            schemaProperties = @SchemaProperty(name = "targetAmounts", array = @ArraySchema(schema = @Schema(implementation = TargetAmountDto.WithTotalSpendingRes.class)))))
    ResponseEntity<?> getTargetAmountsAndTotalSpendings(@Validated TargetAmountDto.GetParamReq param, @AuthenticationPrincipal SecurityUserDetails user);
}
