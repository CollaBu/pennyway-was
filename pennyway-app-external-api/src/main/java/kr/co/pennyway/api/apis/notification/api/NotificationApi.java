package kr.co.pennyway.api.apis.notification.api;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.notification.dto.NotificationDto;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "[알림 API]")
public interface NotificationApi {
    @Operation(summary = "수신한 알림 목록 무한 스크롤 조회")
    @Parameters({
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "조회하려는 페이지 (0..N) (기본 값 : 0)",
                    name = "page",
                    example = "0",
                    schema = @Schema(
                            type = "integer",
                            defaultValue = "0"
                    )
            ),
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "페이지 내 데이터 수 (기본 값 : 30)",
                    name = "size",
                    example = "30",
                    schema = @Schema(
                            type = "integer",
                            defaultValue = "30"
                    )
            ),
            @Parameter(
                    in = ParameterIn.QUERY,
                    description = "정렬 기준 (기본 값 : notification.createdAt,DESC)",
                    name = "sort",
                    example = "notification.createdAt,DESC",
                    array = @ArraySchema(
                            schema = @Schema(
                                    type = "string"
                            )
                    )
            ), @Parameter(name = "pageable", hidden = true)})
    @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공", content = @Content(schemaProperties = @SchemaProperty(name = "notifications", schema = @Schema(implementation = NotificationDto.SliceRes.class))))
    ResponseEntity<?> getNotifications(
            @PageableDefault(page = 0, size = 30) @SortDefault(sort = "notification.createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal SecurityUserDetails user
    );
}
