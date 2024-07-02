package kr.co.pennyway.api.apis.ledger.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.ledger.dto.SpendingCategoryDto;
import kr.co.pennyway.api.apis.ledger.dto.SpendingSearchRes;
import kr.co.pennyway.api.common.query.SpendingCategoryType;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "지출 카테고리 API")
public interface SpendingCategoryApi {
    @Operation(summary = "지출 내역 카테고리 등록", method = "POST", description = "사용자 커스텀 지출 카테고리를 생성합니다.")
    @Parameters({
            @Parameter(name = "name", description = "카테고리 이름", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "icon", description = "카테고리 아이콘. 대문자만 허용합니다.", required = true, in = ParameterIn.QUERY, examples = {
                    @ExampleObject(name = "식사", value = "FOOD"), @ExampleObject(name = "교통", value = "TRANSPORTATION"), @ExampleObject(name = "뷰티/패션", value = "BEAUTY_OR_FASHION"),
                    @ExampleObject(name = "편의점/마트", value = "CONVENIENCE_STORE"), @ExampleObject(name = "교육", value = "EDUCATION"), @ExampleObject(name = "생활", value = "LIVING"),
                    @ExampleObject(name = "건강", value = "HEALTH"), @ExampleObject(name = "취미/여가", value = "HOBBY"), @ExampleObject(name = "여행/숙박", value = "TRAVEL"),
                    @ExampleObject(name = "술/유흥", value = "ALCOHOL_OR_ENTERTAINMENT"), @ExampleObject(name = "회비/경조사", value = "MEMBERSHIP_OR_FAMILY_EVENT"), @ExampleObject(name = "기타", value = "OTHER")
            }),
            @Parameter(name = "param", hidden = true)
    })
    @ApiResponse(responseCode = "200", description = "지출 카테고리 등록 성공", content = @Content(mediaType = "application/json", schemaProperties = @SchemaProperty(name = "spendingCategory", schema = @Schema(implementation = SpendingCategoryDto.Res.class))))
    ResponseEntity<?> postSpendingCategory(@Validated SpendingCategoryDto.CreateParamReq param, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "사용자 정의 지출 카테고리 조회", method = "GET", description = "사용자가 생성한 지출 카테고리 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "지출 카테고리 조회 성공", content = @Content(mediaType = "application/json", schemaProperties = @SchemaProperty(name = "spendingCategories", array = @ArraySchema(schema = @Schema(implementation = SpendingCategoryDto.Res.class)))))
    ResponseEntity<?> getSpendingCategories(@AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "지출 카테고리에 등록된 지출 내역 조회", method = "GET", description = "지출 카테고리별 지출 내역을 조회하며, 무한 스크롤 응답이 반환됩니다.")
    @Parameters({
            @Parameter(name = "categoryId", description = "type이 default면 아이콘 코드(1~11), custom이면 카테고리 pk", required = true, in = ParameterIn.PATH),
            @Parameter(name = "type", description = "지출 카테고리 타입", required = true, in = ParameterIn.QUERY, examples = {
                    @ExampleObject(name = "기본", value = "default"), @ExampleObject(name = "사용자 정의", value = "custom")
            }),
            @Parameter(name = "size", description = "페이지 사이즈 (default: 30)", example = "30", in = ParameterIn.QUERY),
            @Parameter(name = "page", description = "페이지 번호 (default: 0)", example = "0", in = ParameterIn.QUERY),
            @Parameter(name = "sort", description = "정렬 기준 (default: sending.spendAt)", example = "spending.spendAt", in = ParameterIn.QUERY),
            @Parameter(name = "direction", description = "정렬 방식 (default: DESC)", example = "DESC", in = ParameterIn.QUERY),
            @Parameter(name = "pageable", hidden = true)
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "지출 내역 조회 성공", content = @Content(mediaType = "application/json", schemaProperties = @SchemaProperty(name = "spendings", schema = @Schema(implementation = SpendingSearchRes.MonthSlice.class)))),
            @ApiResponse(responseCode = "400", description = "type과 categoryId 미스 매치", content = @Content(examples =
            @ExampleObject(name = "type과 categoryId가 유효하지 않은 조합", description = "type이 default면서, categoryId가 CUSTOM(0) 혹은 OTHER(12)일 수는 없다.", value = """
                        {
                            "code": "4005",
                            "message": "type의 정보와 categoryId의 정보가 존재할 수 없는 조합입니다."
                        }
                    """
            )))
    })
    ResponseEntity<?> getSpendingsByCategory(
            @PathVariable(value = "categoryId") Long categoryId,
            @RequestParam(value = "type") SpendingCategoryType type,
            @PageableDefault(size = 30, page = 0) @SortDefault(sort = "spending.spendAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal SecurityUserDetails user
    );
}
