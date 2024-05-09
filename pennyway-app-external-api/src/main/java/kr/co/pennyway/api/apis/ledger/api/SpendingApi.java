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
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.ledger.dto.SpendingCategoryDto;
import kr.co.pennyway.api.apis.ledger.dto.SpendingSearchRes;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "지출 내역 API")
public interface SpendingApi {
    @Operation(summary = "지출 내역 카테고리 등록", method = "POST", description = "사용자 커스텀 지출 카테고리를 생성합니다.")
    @Parameters({
            @Parameter(name = "name", description = "카테고리 이름", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "icon", description = "카테고리 아이콘", required = true, in = ParameterIn.QUERY, examples = {
                    @ExampleObject(name = "식사", value = "FOOD"), @ExampleObject(name = "교통", value = "TRANSPORTATION"), @ExampleObject(name = "뷰티/패션", value = "BEAUTY_OR_FASHION"),
                    @ExampleObject(name = "편의점/마트", value = "CONVENIENCE_STORE"), @ExampleObject(name = "교육", value = "EDUCATION"), @ExampleObject(name = "생활", value = "LIVING"),
                    @ExampleObject(name = "건강", value = "HEALTH"), @ExampleObject(name = "취미/여가", value = "HOBBY"), @ExampleObject(name = "여행/숙박", value = "TRAVEL"),
                    @ExampleObject(name = "술/유흥", value = "ALCOHOL_OR_ENTERTAINMENT"), @ExampleObject(name = "회비/경조사", value = "MEMBERSHIP_OR_FAMILY_EVENT")
            })
    })
    ResponseEntity<?> postSpendingCategory(@Validated SpendingCategoryDto.CreateParamReq param, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "지출 내역 조회", method = "GET", description = "사용자의 해당 년/월 지출 내역을 조회하고 월/일별 지출 총합을 반환합니다.")
    @Parameters({
            @Parameter(name = "year", description = "년도", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "month", description = "월", required = true, in = ParameterIn.HEADER)
    })
    @ApiResponse(responseCode = "200", content = @Content(schemaProperties = @SchemaProperty(name = "spendings", schema = @Schema(implementation = SpendingSearchRes.Month.class))))
    ResponseEntity<?> getSpendingListAtYearAndMonth(@RequestParam("year") int year, @RequestParam("date") int month, @AuthenticationPrincipal SecurityUserDetails user);
}
