package kr.co.pennyway.api.common.security.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.type.Role;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;

@Getter
public class SecurityUserDetails implements UserDetails {
    private final Long userId;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean accountNonLocked;

    @JsonIgnore
    private boolean enabled;
    @JsonIgnore
    private String password;
    @JsonIgnore
    private boolean credentialsNonExpired;
    @JsonIgnore
    private boolean accountNonExpired;

    @Builder
    private SecurityUserDetails(Long userId, String username, Collection<? extends GrantedAuthority> authorities, boolean accountNonLocked) {
        this.userId = userId;
        this.username = username;
        this.authorities = authorities;
        this.accountNonLocked = accountNonLocked;
    }

    public static UserDetails from(User user) {
        return SecurityUserDetails.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .authorities(Arrays.stream(Role.values())
                        .filter(roleType -> roleType == user.getRole())
                        .map(roleType -> (GrantedAuthority) roleType::getType)
                        .toList())
                .accountNonLocked(user.getLocked())
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEnabled() {
        throw new UnsupportedOperationException();
    }
}
