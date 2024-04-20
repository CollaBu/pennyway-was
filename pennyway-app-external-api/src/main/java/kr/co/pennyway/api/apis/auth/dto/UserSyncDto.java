package kr.co.pennyway.api.apis.auth.dto;

public record UserSyncDto(
        boolean isSignUpAllowed,
        boolean isExistAccount,
        String username
) {
    public static UserSyncDto of(boolean isSignUpAllowed, boolean isExistAccount, String username) {
        return new UserSyncDto(isSignUpAllowed, isExistAccount, username);
    }
}
