package kr.co.pennyway.api.apis.auth.dto;

import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.user.domain.User;

/**
 * 전화번호 검증 후, 시나리오 분기 정보를 위한 DTO
 */
public record UserSyncDto(
        /* isSignUpAllowed가 false인 경우, 반드시 예외를 던지도록 처리해야 한다. */
        boolean isSignUpAllowed,
        boolean isExistAccount,
        Long userId,
        String username,
        /* 계정과 연동하기 위한 oauth 정보. 없다면 null */
        OauthSync oauthSync
) {
    /**
     * @param isSignUpAllowed boolean : 회원가입 시나리오 가능 여부 (true: 회원가입 혹은 계정 연동 가능, false: 불가능)
     * @param isExistAccount  boolean : 이미 존재하는 계정 여부
     * @param user            {@link User} : 사용자 정보
     * @param oauthSync       {@link OauthSync} : 연동할 Oauth 정보. 없다면 null
     */
    public static UserSyncDto of(boolean isSignUpAllowed, boolean isExistAccount, User user, OauthSync oauthSync) {
        return new UserSyncDto(isSignUpAllowed, isExistAccount, user.getId(), user.getUsername(), oauthSync);
    }

    /**
     * 이미 회원이 존재하는 경우 사용하는 편의용 메서드. <br/>
     * 내부에서 {@link UserSyncDto#of(boolean, boolean, User, OauthSync)}를 호출한다.
     *
     * @param user {@link User} : 사용자 정보
     */
    public static UserSyncDto abort(User user) {
        return UserSyncDto.of(false, true, user, null);
    }

    public record OauthSync(
            Long id,
            String oauthId,
            Provider provider
    ) {
        public static OauthSync of(Long id, String oauthId, Provider provider) {
            return new OauthSync(id, oauthId, provider);
        }
    }
}
