package kr.co.pennyway.api.common.security.authentication;

import kr.co.pennyway.domain.context.account.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {
    private final UserService userService;

    @Override
    @Cacheable(value = "securityUser", key = "#userId", unless = "#result == null", cacheManager = "securityUserCacheManager")
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return userService.readUser(Long.parseLong(userId))
                .map(SecurityUserDetails::from)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }
}
