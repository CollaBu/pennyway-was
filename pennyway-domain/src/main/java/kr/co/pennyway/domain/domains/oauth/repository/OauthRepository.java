package kr.co.pennyway.domain.domains.oauth.repository;

import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthRepository extends JpaRepository<Oauth, Long> {
}
