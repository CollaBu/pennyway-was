package kr.co.pennyway.api.common.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SuccessResponseTest {

  private TestDto dto;

  @BeforeEach
  void setUp() {
    dto = new TestDto("test", 1);
  }

  @Test
  @DisplayName("SuccessResponse.from() - data가 존재하는 성공 응답")
  public void successResponseWithData() {
    // Given
    String key = "example";
    String value = "data";

    // When
    SuccessResponse<?> response = SuccessResponse.from(key, value);

    // Then
    assertEquals("2000", response.getCode());
    assertEquals(Map.of(key, value), response.getData());
  }

  @Test
  @DisplayName("SuccessResponse.from() - data가 존재하는 성공 응답")
  public void successResponseWithNoContent() {
    // When
    SuccessResponse<?> response = SuccessResponse.noContent();

    // Then
    assertEquals("2000", response.getCode());
    assertEquals(Map.of(), response.getData());
  }

  @Test
  @DisplayName("SuccessResponse.from() - DTO를 통한 성공 응답")
  public void successResponseFromDto() {
    // When
    SuccessResponse<TestDto> response = SuccessResponse.from(dto);

    // Then
    assertEquals("2000", response.getCode());
    assertEquals(dto, response.getData());
    System.out.println(response);
  }

  @Test
  @DisplayName("SuccessResponse.from() - key와 DTO를 통한 성공 응답")
  public void successResponseFromDtoWithKey() {
    // Given
    String key = "test";

    // When
    SuccessResponse<Map<String, TestDto>> response = SuccessResponse.from(key, dto);

    // Then
    assertEquals("2000", response.getCode());
    assertEquals(Map.of(key, dto), response.getData());
    System.out.println(response);
  }

  private record TestDto(String name, int age) {

  }
}
