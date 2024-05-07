package kr.co.pennyway.domain.config;

import kr.co.pennyway.domain.DomainPackageLocation;
import kr.co.pennyway.domain.common.repository.QueryDslSearchRepositoryFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EntityScan(basePackageClasses = DomainPackageLocation.class)
@EnableJpaRepositories(basePackageClasses = DomainPackageLocation.class, repositoryFactoryBeanClass = QueryDslSearchRepositoryFactory.class)
public class JpaConfig {
}
