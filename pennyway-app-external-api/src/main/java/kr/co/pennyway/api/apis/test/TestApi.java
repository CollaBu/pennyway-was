package kr.co.pennyway.api.apis.test;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import kr.co.pennyway.api.common.response.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestApi {
    @ApiResponse(responseCode = "200", description = "회원가입 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class)))
    @GetMapping("/no-content")
    public ResponseEntity<?> noContent() {
        return ResponseEntity.ok(SuccessResponse.noContent());
    }

    @GetMapping("/data1")
    public ResponseEntity<?> data1() {
        return ResponseEntity.ok(SuccessResponse.from("key", "value"));
    }

    @GetMapping("/data2")
    public ResponseEntity<?> data2() {
        TestDto dto = new TestDto("test", 1);
        SuccessResponse<Map<String, TestDto>> response = SuccessResponse.from("domain", dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/data3")
    public ResponseEntity<?> data3() {
        TestDto dto = new TestDto("test", 1);
        return ResponseEntity.ok(SuccessResponse.from(dto));
    }

    @GetMapping("/data4")
    public ResponseEntity<?> data4() {
        TestDto dto = new TestDto("test", 1);
        return ResponseEntity.ok(SuccessResponse.from(Map.of("domain", dto)));
    }

    private record TestDto(String name, int age) {
    }
}
