package kr.co.pennyway.domain.domains.spending.dto;

import kr.co.pennyway.domain.domains.spending.type.SpendingCategory;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 지출 카테고리 정보를 담은 DTO
 *
 * @param isCustom boolean : 사용자 정의 카테고리 여부
 * @param id       Long : 카테고리 ID. 사용자 정의 카테고리가 아니라면 -1, 사용자 정의 카테고리라면 0 이상의 값을 갖는다.
 * @param name     String : 카테고리 이름
 * @param icon     String : 카테고리 아이콘
 */
public record CategoryInfo(
        boolean isCustom,
        Long id,
        String name,
        SpendingCategory icon
) {
    public CategoryInfo {
        Objects.requireNonNull(id, "id는 null일 수 없습니다.");
        Objects.requireNonNull(icon, "icon은 null일 수 없습니다.");

        if (isCustom && id < 0 || !isCustom && id != -1) {
            throw new IllegalArgumentException("isCustom과 id 정보가 일치하지 않습니다.");
        }

        if (isCustom && icon.equals(SpendingCategory.OTHER)) {
            throw new IllegalArgumentException("사용자 정의 카테고리는 OTHER가 될 수 없습니다.");
        }

        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("name은 null이거나 빈 문자열일 수 없습니다.");
        }
    }

    public static CategoryInfo of(Long id, String name, SpendingCategory icon) {
        return new CategoryInfo(id != null, id, name, icon);
    }
}
