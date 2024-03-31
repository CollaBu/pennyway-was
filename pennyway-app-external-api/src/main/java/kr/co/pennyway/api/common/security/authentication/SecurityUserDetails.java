package kr.co.pennyway.api.common.security.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityUserDetails implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean accountNonLocked;

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
                .authorities(List.of(new CustomGrantedAuthority(user.getRole().getType())))
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

    @Override
    public String toString() {
        return "SecurityUserDetails{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", authorities=" + authorities +
                ", accountNonLocked=" + accountNonLocked +
                '}';
    }

}
