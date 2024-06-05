package kr.co.pennyway.api.apis.ledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.co.pennyway.domain.domains.spending.dto.CategoryInfo;
import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import org.springframework.util.StringUtils;

import java.util.Objects;

public class SpendingCategoryDto {
    public record CreateParamReq(
            @NotBlank(message = "카테고리 이름은 필수입니다.")
            @Size(max = 15, message = "카테고리 이름은 15자 이하로 입력해주세요.")
            String name,
            @NotNull(message = "카테고리 아이콘은 필수입니다.")
            SpendingCategory icon
    ) {
    }

    @Schema(title = "지출 카테고리 정보")
    public record Res(
            @Schema(description = "사용자 정의 카테고리 여부")
            boolean isCustom,
            @Schema(description = "카테고리 ID. 사용자 정의 카테고리가 아니라면 -1, 사용자 정의 카테고리라면 0 이상의 값을 갖는다.")
            Long id,
            @Schema(description = "카테고리 이름")
            String name,
            @Schema(description = "카테고리 아이콘", example = "FOOD", examples = {"FOOD", "TRANSPORTATION", "BEAUTY_OR_FASHION", "CONVENIENCE_STORE", "EDUCATION", "LIVING", "HEALTH", "HOBBY", "TRAVEL", "ALCOHOL_OR_ENTERTAINMENT", "MEMBERSHIP_OR_FAMILY_EVENT"})
            SpendingCategory icon
    ) {
        public Res {
            Objects.requireNonNull(id, "id는 null일 수 없습니다.");
            Objects.requireNonNull(icon, "icon은 null일 수 없습니다.");

            if (isCustom && id < 0 || !isCustom && id != -1) {
                throw new IllegalArgumentException("isCustom과 id 정보가 일치하지 않습니다.");
            }

            if (isCustom && icon.equals(SpendingCategory.CUSTOM)) {
                throw new IllegalArgumentException("사용자 정의 카테고리는 CUSTOM이 될 수 없습니다.");
            }

            if (!isCustom && (icon.equals(SpendingCategory.CUSTOM) || icon.equals(SpendingCategory.OTHER))) {
                throw new IllegalArgumentException("서비스에서 제공하는 카테고리는 CUSTOM 혹은 OTHER이 될 수 없습니다.");
            }

            if (!StringUtils.hasText(name)) {
                throw new IllegalArgumentException("name은 null이거나 빈 문자열일 수 없습니다.");
            }
        }

        public static Res from(CategoryInfo category) {
            return new Res(category.isCustom(), category.id(), category.name(), category.icon());
        }
    }
}
