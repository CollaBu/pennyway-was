package kr.co.pennyway.api.apis.notification.api;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.notification.dto.NotificationDto;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "[알림 API]")
public interface NotificationApi {
    @Operation(summary = "수신한 읽음 알림 목록 무한 스크롤 조회")
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
    ResponseEntity<?> getReadNotifications(
            @PageableDefault(page = 0, size = 30) @SortDefault(sort = "notification.createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal SecurityUserDetails user
    );

    @Operation(summary = "수신한 미확인 알림 목록 조회")
    @ApiResponse(responseCode = "200", description = "미확인 알림 목록 조회 성공", content = @Content(schemaProperties = @SchemaProperty(name = "notifications", schema = @Schema(implementation = NotificationDto.ListRes.class))))
    ResponseEntity<?> getUnreadNotifications(@AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "수신한 알림 중 미확인 알림 존재 여부 조회")
    @ApiResponse(responseCode = "200", description = "미확인 알림 존재 여부 조회 성공", content = @Content(schemaProperties = @SchemaProperty(name = "hasUnread", schema = @Schema(type = "boolean"))))
    ResponseEntity<?> getHasUnreadNotification(@AuthenticationPrincipal SecurityUserDetails user);

    @Operation(summary = "수신한 알림 읽음 처리", description = "사용자가 수신한 알림을 읽음처리 합니다. 단, 읽음 처리할 알림의 pk는 사용자가 receiver여야 하며, 미확인 알림만 포함되어 있어야 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공"),
            @ApiResponse(responseCode = "403", description = "사용자가 접근할 권한이 없는 pk가 포함되어 있거나, 이미 읽음 처리된 알림이 하나라도 존재하는 경우", content = @Content(examples =
            @ExampleObject("""
                    {
                      "code": "4030",
                      "message": "ACCESS_TO_THE_REQUESTED_RESOURCE_IS_FORBIDDEN"
                    }
                    """)
            ))
    })
    ResponseEntity<?> updateNotifications(@RequestBody @Validated NotificationDto.ReadReq readReq);
}
