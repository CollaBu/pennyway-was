package kr.co.pennyway.api.apis.ledger.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.ledger.dto.TargetAmountDto;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "목표금액 API")
public interface TargetAmountApi {
    @Operation(summary = "당월 목표 금액 더미값 생성", method = "POST", description = "더미값 생성을 위한 API이며, 사용자가 당월 첫 로그인 시 클라이언트에서 호출한다.")
    @Parameters({
            @Parameter(name = "year", description = "생성하려는 목표 금액 년도", required = true, example = "2024", in = ParameterIn.QUERY),
            @Parameter(name = "month", description = "생성하려는 목표 금액 월", required = true, example = "5", in = ParameterIn.QUERY)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목표 금액 데이터 생성", content = @Content(schemaProperties = @SchemaProperty(name = "targetAmount", schema = @Schema(implementation = TargetAmountDto.TargetAmountInfo.class)))),
            @ApiResponse(responseCode = "400", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "목표 금액 삭제 실패", value = """
                            {
                                "code": "4004",
                                "message": "당월 목표 금액에 대한 요청이 아닙니다."
                            }
                            """)})),
            @ApiResponse(responseCode = "409", description = "이미 당월 목표 금액이 존재하는 경우", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "목표 금액 생성 실패", value = """
                            {
                                "code": "4091",
                                "message": "이미 해당 월의 목표 금액 데이터가 존재합니다."
                            }
                            """)}))
    })
    ResponseEntity<?> postTargetAmount(@RequestParam int year, @RequestParam int month, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "임의의 년/월에 대한 목표 금액 및 총 사용 금액 조회", method = "GET", description = "일수는 무시하고 년/월 정보만 사용한다. 일반적으로 당월 정보 요청에 사용하는 API이다.")
    @Parameter(name = "date", description = "현재 날짜(yyyy-MM-dd)", required = true, example = "2024-05-08", in = ParameterIn.PATH)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목표 금액 및 총 사용 금액 조회 성공", content = @Content(
                    schemaProperties = @SchemaProperty(name = "targetAmount", schema = @Schema(implementation = TargetAmountDto.WithTotalSpendingRes.class)))),
            @ApiResponse(responseCode = "404", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "목표 금액 조회 실패", description = "목표 금액 데이터가 존재하지 않는 경우. 클라이언트는 POST 호출 시나리오를 진행해야 한다.", value = """
                            {
                                "code": "4040",
                                "message": "해당 월의 목표 금액이 존재하지 않습니다."
                            }
                            """)}))
    })
    ResponseEntity<?> getTargetAmountAndTotalSpending(@PathVariable LocalDate date, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "가장 오래된 목표 금액 이후부터 현재까지의 목표 금액 및 총 사용 금액 리스트 조회", method = "GET", description = "일수는 무시하고 년/월 정보만 사용한다. 데이터가 존재하지 않을 때 더미 값을 사용하며, 최신 데이터 순으로 정렬된 응답을 반환한다.")
    @Parameters({
            @Parameter(name = "date", description = "현재 날짜(yyyy-MM-dd)", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "param", hidden = true)
    })
    @ApiResponse(responseCode = "200", description = "목표 금액 및 총 사용 금액 리스트 조회 성공", content = @Content(
            schemaProperties = @SchemaProperty(name = "targetAmounts", array = @ArraySchema(schema = @Schema(implementation = TargetAmountDto.WithTotalSpendingRes.class)))))
    ResponseEntity<?> getTargetAmountsAndTotalSpendings(@Validated TargetAmountDto.DateParam param, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "당월 이전 사용자가 입력한 목표 금액 중 최신 데이터 단일 조회", method = "GET",
            description = "당월에 목표 금액이 존재한다면 당월 목표 금액이 반환되겠지만, 일반적으로 해당 API는 당월 목표 금액 조회 시 isRead가 false인 경우이므로 amount도 -1이라는 전제를 두어 별도의 예외처리를 수행하지는 않는다. isPresent 필드를 통해 데이터 존재 여부를 확인할 수 있다.")
    @ApiResponse(responseCode = "200", description = "목표 금액 조회 성공", content = @Content(schemaProperties = @SchemaProperty(name = "targetAmount", schema = @Schema(implementation = TargetAmountDto.RecentTargetAmountRes.class))))
    ResponseEntity<?> getRecentTargetAmount(@AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "당월 목표 금액 수정", method = "PATCH")
    @Parameters({
            @Parameter(name = "targetAmountId", description = "수정하려는 목표 금액 ID", required = true, example = "1", in = ParameterIn.PATH),
            @Parameter(name = "amount", description = "수정하려는 목표 금액", required = true, in = ParameterIn.QUERY, example = "100000"),
            @Parameter(name = "param", hidden = true)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목표 금액 수정 성공", content = @Content(schemaProperties = @SchemaProperty(name = "targetAmount", schema = @Schema(implementation = TargetAmountDto.TargetAmountInfo.class)))),
            @ApiResponse(responseCode = "400", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "목표 금액 수정 실패", description = "해당 월의 목표 금액이 아닌 경우", value = """
                            {
                                "code": "4004",
                                "message": "당월 목표 금액에 대한 요청이 아닙니다."
                            }
                            """)})),
            @ApiResponse(responseCode = "404", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "목표 금액 조회 실패", description = "목표 금액 데이터가 없거나, 이미 삭제(amount=-1)인 경우", value = """
                            {
                                "code": "4040",
                                "message": "해당 월의 목표 금액이 존재하지 않습니다."
                            }
                            """)}))
    })
    ResponseEntity<?> patchTargetAmount(TargetAmountDto.AmountParam param, @PathVariable Long targetAmountId);

    @Operation(summary = "당월 목표 금액 삭제", method = "DELETE")
    @Parameter(name = "targetAmountId", description = "삭제하려는 목표 금액 ID", required = true, example = "1", in = ParameterIn.PATH)
    @ApiResponses({
            @ApiResponse(responseCode = "400", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "목표 금액 삭제 실패", description = "해당 월의 목표 금액이 아닌 경우", value = """
                            {
                                "code": "4004",
                                "message": "당월 목표 금액에 대한 요청이 아닙니다."
                            }
                            """)})),
            @ApiResponse(responseCode = "404", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "목표 금액 삭제 실패", description = "목표 금액 데이터가 없거나, 이미 삭제(amount=-1)인 경우", value = """
                            {
                                "code": "4040",
                                "message": "해당 월의 목표 금액이 존재하지 않습니다."
                            }
                            """)}))
    })
    ResponseEntity<?> deleteTargetAmount(@PathVariable Long targetAmountId);
}
