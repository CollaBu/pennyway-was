package kr.co.pennyway.api.apis.socket.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "[서비스 탐색 API]")
public interface SocketApi {
    @Operation(summary = "연결 가능한 채팅 서버 정보 조회", description = "요청 헤더, 바디를 기반으로 연결 가능한 채팅 서버 정보를 조회한 후, 채팅 서버 정보를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "연결 가능한 채팅 서버 정보 조회 성공", content = @Content(schemaProperties = @SchemaProperty(name = "url", schema = @Schema(type = "string", description = "채팅 서버 URL"))))
    ResponseEntity<?> getChatServerInfo();
}
