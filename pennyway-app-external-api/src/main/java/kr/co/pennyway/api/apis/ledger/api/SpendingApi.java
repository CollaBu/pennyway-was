package kr.co.pennyway.api.apis.ledger.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.ledger.dto.SpendingReq;
import kr.co.pennyway.api.apis.ledger.dto.SpendingSearchRes;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "지출 내역 API")
public interface SpendingApi {
    @Operation(summary = "지출 내역 추가", method = "POST", description = """
            사용자의 지출 내역을 추가하고 추가된 지출 내역을 반환합니다. <br/>
            서비스에서 제공하는 지출 카테고리를 사용하는 경우 categoryId는 -1이어야 하며, icon은 OTHER가 될 수 없습니다. <br/>
            사용자가 정의한 지출 카테고리를 사용하는 경우 categoryId는 -1이 아니어야 하며, icon은 OTHER여야 합니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schemaProperties = @SchemaProperty(name = "spending", schema = @Schema(implementation = SpendingSearchRes.Individual.class)))),
            @ApiResponse(responseCode = "400", description = "지출 카테고리 ID와 아이콘의 조합이 올바르지 않습니다.", content = @Content(examples = {
                    @ExampleObject(name = "카테고리 id, 아이콘 조합 오류", description = "categoryId가 -1인데 icon이 OTHER이거나, categoryId가 -1이 아닌데 icon이 OTHER가 아닙니다.",
                            value = """
                                    {
                                    "code": "4005",
                                    "message": "icon의 정보와 categoryId의 정보가 존재할 수 없는 조합입니다."
                                    }
                                    """
                    )
            })),
            @ApiResponse(responseCode = "403", description = "지출 카테고리에 대한 권한이 없습니다.", content = @Content(examples = {
                    @ExampleObject(name = "지출 카테고리 권한 오류", description = "지출 카테고리에 대한 권한이 없습니다.",
                            value = """
                                    {
                                    "code": "4030",
                                    "message": "ACCESS_TO_THE_REQUESTED_RESOURCE_IS_FORBIDDEN"
                                    }
                                    """
                    )
            }))
    })
    ResponseEntity<?> postSpending(@RequestBody @Validated SpendingReq request, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "지출 내역 조회", method = "GET", description = "사용자의 해당 년/월 지출 내역을 조회하고 월/일별 지출 총합을 반환합니다.")
    @Parameters({
            @Parameter(name = "year", description = "년도", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "month", description = "월", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponse(responseCode = "200", content = @Content(schemaProperties = @SchemaProperty(name = "spendings", schema = @Schema(implementation = SpendingSearchRes.Month.class))))
    ResponseEntity<?> getSpendingListAtYearAndMonth(@RequestParam("year") int year, @RequestParam("date") int month, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "지출 내역 상세 조회", method = "GET", description = "지출 내역의 ID값으로 해당 지출의 상세 내역을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schemaProperties = @SchemaProperty(name = "spending", schema = @Schema(implementation = SpendingSearchRes.Individual.class)))),
            @ApiResponse(responseCode = "404", description = "NOT_FOUND", content = @Content(examples = {
                    @ExampleObject(name = "지출 내역 조회 오류",
                            value = """
                                    {
                                    "code": "4040",
                                    "message": "존재하지 않는 지출 내역입니다."
                                    }
                                    """
                    )
            }))
    })
    ResponseEntity<?> getSpendingDetail(@PathVariable Long spendingId, @AuthenticationPrincipal SecurityUserDetails user);
}
