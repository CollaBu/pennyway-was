package kr.co.pennyway.api.apis.ledger.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.ledger.dto.SpendingCategoryDto;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;

@Tag(name = "지출 카테고리 API")
public interface SpendingCategoryApi {
    @Operation(summary = "지출 내역 카테고리 등록", method = "POST", description = "사용자 커스텀 지출 카테고리를 생성합니다.")
    @Parameters({
            @Parameter(name = "name", description = "카테고리 이름", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "icon", description = "카테고리 아이콘. 대문자만 허용합니다.", required = true, in = ParameterIn.QUERY, examples = {
                    @ExampleObject(name = "식사", value = "FOOD"), @ExampleObject(name = "교통", value = "TRANSPORTATION"), @ExampleObject(name = "뷰티/패션", value = "BEAUTY_OR_FASHION"),
                    @ExampleObject(name = "편의점/마트", value = "CONVENIENCE_STORE"), @ExampleObject(name = "교육", value = "EDUCATION"), @ExampleObject(name = "생활", value = "LIVING"),
                    @ExampleObject(name = "건강", value = "HEALTH"), @ExampleObject(name = "취미/여가", value = "HOBBY"), @ExampleObject(name = "여행/숙박", value = "TRAVEL"),
                    @ExampleObject(name = "술/유흥", value = "ALCOHOL_OR_ENTERTAINMENT"), @ExampleObject(name = "회비/경조사", value = "MEMBERSHIP_OR_FAMILY_EVENT")
            }),
            @Parameter(name = "param", hidden = true)
    })
    @ApiResponse(responseCode = "200", description = "지출 카테고리 등록 성공", content = @Content(mediaType = "application/json", schemaProperties = @SchemaProperty(name = "spendingCategory", schema = @Schema(implementation = SpendingCategoryDto.Res.class))))
    ResponseEntity<?> postSpendingCategory(@Validated SpendingCategoryDto.CreateParamReq param, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "사용자 정의 지출 카테고리 조회", method = "GET", description = "사용자가 생성한 지출 카테고리 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "지출 카테고리 조회 성공", content = @Content(mediaType = "application/json", schemaProperties = @SchemaProperty(name = "spendingCategories", array = @ArraySchema(schema = @Schema(implementation = SpendingCategoryDto.Res.class)))))
    ResponseEntity<?> getSpendingCategories(@AuthenticationPrincipal SecurityUserDetails user);
}
