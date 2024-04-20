package kr.co.pennyway.api.apis.auth.dto;

/**
 * 전화번호 검증 후, 시나리오 분기 정보를 위한 DTO
 */
public record UserSyncDto(
        boolean isSignUpAllowed,
        boolean isExistAccount,
        String username
) {
    /**
     * @param isSignUpAllowed boolean : 회원가입 시나리오 가능 여부 (true: 회원가입 혹은 계정 연동 가능, false: 불가능)
     * @param isExistAccount  boolean : 이미 존재하는 계정 여부
     * @param username        boolean : 사용자명 (isExistAccount이 true인 경우에만 존재)
     */
    public static UserSyncDto of(boolean isSignUpAllowed, boolean isExistAccount, String username) {
        return new UserSyncDto(isSignUpAllowed, isExistAccount, username);
    }
}
