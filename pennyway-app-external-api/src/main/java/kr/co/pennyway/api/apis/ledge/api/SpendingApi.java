package kr.co.pennyway.api.apis.ledge.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "지출 내역 API")
public interface SpendingApi {
    @Operation(summary = "지출 내역 조회", method = "GET")
    @Parameters({
            @Parameter(name = "year", description = "년도", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "month", description = "월", required = true, in = ParameterIn.HEADER)
    })
    ResponseEntity<?> getSpendingListAtMonth(@RequestParam("year") int year, @RequestParam("date") int month, @AuthenticationPrincipal SecurityUserDetails user);
}
