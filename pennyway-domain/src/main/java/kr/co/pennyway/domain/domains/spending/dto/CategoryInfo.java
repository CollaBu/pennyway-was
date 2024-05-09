package kr.co.pennyway.domain.domains.spending.dto;

import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 지출 카테고리 정보를 담은 DTO
 *
 * @param name : 카테고리 이름. null 불가
 * @param icon : 카테고리 아이콘. null 불가
 */
public record CategoryInfo(
        Long id,
        String name,
        String icon
) {
    public CategoryInfo {
        Objects.requireNonNull(id, "id은 null일 수 없습니다.");
        if (!StringUtils.hasText(name) || !StringUtils.hasText(icon)) {
            throw new IllegalArgumentException("name, icon은 null이거나 빈 문자열일 수 없습니다.");
        }
    }

    public static CategoryInfo of(Long id, String name, String icon) {
        return new CategoryInfo(id, name, icon);
    }
}
