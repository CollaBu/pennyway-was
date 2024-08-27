package kr.co.pennyway.api.apis.auth.dto;

/**
 * 전화번호 검증 후, 시나리오 분기 정보를 위한 DTO
 */
public record UserSyncDto(
        /* isSignUpAllowed가 false인 경우, 반드시 예외를 던지도록 처리해야 한다. */
        boolean isSignUpAllowed,
        boolean isExistAccount,
        Long userId,
        String username
) {
    /**
     * @param isSignUpAllowed boolean : 회원가입 시나리오 가능 여부 (true: 회원가입 혹은 계정 연동 가능, false: 불가능)
     * @param isExistAccount  boolean : 이미 존재하는 계정 여부
     * @param userId          Long : 사용자 ID. 없다면 null
     * @param username        String : 사용자 이름. 없다면 null
     */
    public static UserSyncDto of(boolean isSignUpAllowed, boolean isExistAccount, Long userId, String username) {
        return new UserSyncDto(isSignUpAllowed, isExistAccount, userId, username);
    }

    /**
     * 이미 회원이 존재하는 경우 사용하는 편의용 메서드. <br/>
     * 내부에서 {@link UserSyncDto#of(boolean, boolean, Long, String)}를 호출한다.
     */
    public static UserSyncDto abort(Long userId, String username) {
        return UserSyncDto.of(false, true, userId, username);
    }

    /**
     * 회원 가입 이력이 없는 경우 사용하는 편의용 메서드. <br/>
     * 내부에서 {@link UserSyncDto#of(boolean, boolean, Long, String)}를 호출한다.
     */
    public static UserSyncDto signUpAllowed() {
        return UserSyncDto.of(true, false, null, null);
    }
}
