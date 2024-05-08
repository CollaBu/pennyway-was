package kr.co.pennyway.api.apis.ledge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class TargetAmountDto {
    public record UpdateParamReq(
            @NotNull(message = "date 값은 필수입니다.")
            @JsonSerialize(using = LocalDateSerializer.class)
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate date,
            @NotNull(message = "amount 값은 필수입니다.")
            @Min(value = 0, message = "amount 값은 0 이상이어야 합니다.")
            Integer amount
    ) {

    }
}
