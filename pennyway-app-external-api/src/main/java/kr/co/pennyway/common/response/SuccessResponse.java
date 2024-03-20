package kr.co.pennyway.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

/**
 * API Response의 success에 대한 공통적인 응답을 정의한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "API 응답 - 성공")
public class SuccessResponse<T> {
    @Schema(description = "응답 코드", defaultValue = "2000000")
    private final String code = "2000000";
    @Schema(description = "응답 메시지", example = """
            data: {
                "aDomain": { // 단수명사는 object 형태로 반환
                    ...
                },`
                "bDomains": [ // 복수명사는 array 형태로 반환
                    ...
                ]
            }
            """)
    private T data;

    @Builder
    private SuccessResponse(T data) {this.data = data;}

    /**
     * data : { "key" : data } 형태의 성공 응답을 반환한다.
     * <br/>
     * 명시적으로 key의 이름을 지정하기 위해 사용한다.
     */
    public static <V> SuccessResponse<Map<String, V>> from(String key, V data) {
        return SuccessResponse.<Map<String, V>>builder()
                .data(Map.of(key, data))
                .build();
    }

    public static <T> SuccessResponse<T> from(T data) {
        return SuccessResponse.<T>builder()
                .data(data)
                .build();
    }

    /**
     * data가 null인 경우 사용한다.
     * <br/>
     * data : {} 형태의 성공 응답을 반환한다.
     */
    public static SuccessResponse<?> noContent() {
        return SuccessResponse.builder().data(Map.of()).build();
    }

    @Override
    public String toString() {
        return "SuccessResponse{" +
                "code='" + code + '\'' +
                ", data=" + data +
                '}';
    }
}
