package kr.co.pennyway.api.config.supporter;

import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.api.config.fixture.UserFixture;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithCustomMockUserSecurityContextFactory implements WithSecurityContextFactory<WithSecurityMockUser> {
    @Override
    public SecurityContext createSecurityContext(WithSecurityMockUser annotation) {
        String userId = annotation.userId();

        SecurityUserDetails user = UserFixture.createSecurityUser(Long.parseLong(userId));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
