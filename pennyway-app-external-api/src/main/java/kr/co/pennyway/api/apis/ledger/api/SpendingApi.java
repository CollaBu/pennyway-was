package kr.co.pennyway.api.apis.ledger.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.ledger.dto.SpendingIdsDto;
import kr.co.pennyway.api.apis.ledger.dto.SpendingReq;
import kr.co.pennyway.api.apis.ledger.dto.SpendingSearchRes;
import kr.co.pennyway.api.apis.ledger.dto.SpendingShareReq;
import kr.co.pennyway.api.common.annotation.ApiExceptionExplanation;
import kr.co.pennyway.api.common.annotation.ApiResponseExplanations;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.domain.domains.spending.exception.SpendingErrorCode;
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
            서비스에서 제공하는 지출 카테고리를 사용하는 경우 categoryId는 -1이어야 하며, icon은 CUSTOM 혹은 OTHER이 될 수 없습니다. <br/>
            사용자가 정의한 지출 카테고리를 사용하는 경우 categoryId는 -1이 아니어야 하며, icon은 CUSTOM이여야 합니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schemaProperties = @SchemaProperty(name = "spending", schema = @Schema(implementation = SpendingSearchRes.Individual.class)))),
            @ApiResponse(responseCode = "400", description = "지출 카테고리 ID와 아이콘의 조합이 올바르지 않습니다.", content = @Content(examples = {
                    @ExampleObject(name = "카테고리 id, 아이콘 조합 오류", description = "categoryId가 -1인데 icon이 CUSTOM/OTHER이거나, categoryId가 -1이 아닌데 icon이 CUSTOM이 아닙니다.",
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
            @Parameter(name = "year", description = "년도", example = "2024", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "month", description = "월", example = "5", required = true, in = ParameterIn.QUERY)
    })
    @ApiResponse(responseCode = "200", content = @Content(schemaProperties = @SchemaProperty(name = "spending", schema = @Schema(implementation = SpendingSearchRes.Month.class))))
    ResponseEntity<?> getSpendingListAtYearAndMonth(@RequestParam("year") int year, @RequestParam("date") int month, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "지출 내역 상세 조회", method = "GET", description = "지출 내역의 ID값으로 해당 지출의 상세 내역을 반환합니다.")
    @Parameter(name = "spendingId", description = "지출 내역 ID", example = "1", required = true, in = ParameterIn.PATH)
    @ApiResponse(responseCode = "200", content = @Content(schemaProperties = @SchemaProperty(name = "spending", schema = @Schema(implementation = SpendingSearchRes.Individual.class))))
    @ApiResponseExplanations(
            errors = {
                    @ApiExceptionExplanation(name = "지출 내역 조회 오류", description = "NOT_FOUND", value = SpendingErrorCode.class, constant = "NOT_FOUND_SPENDING")
            }
    )
    ResponseEntity<?> getSpendingDetail(@PathVariable Long spendingId, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "지출 내역 수정", method = "PUT", description = """
            사용자의 지출 내역을 수정하고 수정된 지출 내역을 반환합니다. <br/>
            서비스에서 제공하는 지출 카테고리를 사용하는 경우 categoryId는 -1이어야 하며, icon은 OTHER가 될 수 없습니다. <br/>
            사용자가 정의한 지출 카테고리를 사용하는 경우 categoryId는 -1이 아니어야 하며, icon은 OTHER여야 합니다.
            """)
    @ApiResponse(responseCode = "200", content = @Content(schemaProperties = @SchemaProperty(name = "spending", schema = @Schema(implementation = SpendingSearchRes.Individual.class))))
    ResponseEntity<?> updateSpending(@PathVariable Long spendingId, @RequestBody @Validated SpendingReq request, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "지출 내역 삭제", method = "DELETE", description = "지출 내역의 ID값으로 해당 지출 내역을 삭제 합니다.")
    @Parameter(name = "spendingId", description = "지출 내역 ID", example = "1", required = true, in = ParameterIn.PATH)
    @ApiResponse(responseCode = "403", description = "지출 내역에 대한 권한이 없습니다.", content = @Content(examples = {
            @ExampleObject(name = "지출 내역 권한 오류", description = "지출 내역에 대한 권한이 없습니다.",
                    value = """
                            {
                            "code": "4030",
                            "message": "ACCESS_TO_THE_REQUESTED_RESOURCE_IS_FORBIDDEN"
                            }
                            """
            )
    }))
    ResponseEntity<?> deleteSpending(@PathVariable Long spendingId, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "지출 내역 복수 삭제", method = "DELETE", description = "사용자의 지출 내역의 ID목록으로 해당 지출 내역들을 삭제 합니다.")
    @ApiResponse(responseCode = "403", description = "지출 내역에 대한 권한이 없습니다.", content = @Content(examples = {
            @ExampleObject(name = "지출 내역 권한 오류", description = "지출 내역에 대한 권한이 없습니다.",
                    value = """
                            {
                            "code": "4030",
                            "message": "ACCESS_TO_THE_REQUESTED_RESOURCE_IS_FORBIDDEN"
                            }
                            """
            )
    }))
    ResponseEntity<?> deleteSpendings(@RequestBody SpendingIdsDto spendingIds, @AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "지출 내역 공유", method = "GET", description = """
            사용자의 지출 내역을 공유하고 공유된 지출 내역을 반환합니다. <br/>
            <채팅방 공유 시> 전송할 채팅방 아이디를 누락한 경우 예외를 발생시키지만, 가입하지 않은 채팅방 아이디를 전송한 경우는 유효한 방에만 전송하고 별도의 예외를 발생시키지 않습니다.
            """)
    @Parameters({
            @Parameter(name = "type", description = "공유할 목적지(타입)", required = true, in = ParameterIn.QUERY, examples = {
                    @ExampleObject(name = "채팅방 공유", value = "CHAT_ROOM")
            }),
            @Parameter(name = "year", description = "년도", example = "2025", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "month", description = "월", example = "1", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "day", description = "일", example = "28", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "chatRoomIds", description = "공유할 채팅방 ID 목록 배열 (채팅방 공유 시, null 혹은 빈 배열 허용하지 않음.)", in = ParameterIn.QUERY, array = @ArraySchema(schema = @Schema(type = "long"))),
            @Parameter(name = "query", hidden = true)
    })
    @ApiResponseExplanations(errors = {
            @ApiExceptionExplanation(name = "전송 타입 오류", description = "유효하지 않은 목적지로 지출 내용을 공유할 수 없습니다.", value = SpendingErrorCode.class, constant = "INVALID_SHARE_TYPE"),
            @ApiExceptionExplanation(name = "채팅방 공유 파라미터 누락", description = "지출 내역 공유 시 필수 파라미터가 누락되었습니다.", value = SpendingErrorCode.class, constant = "MISSING_SHARE_PARAM")
    })
    @ApiResponse(responseCode = "200")
    ResponseEntity<?> shareSpending(@Validated SpendingShareReq.ShareQueryParam query, @AuthenticationPrincipal SecurityUserDetails user);
}
