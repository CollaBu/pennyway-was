package kr.co.pennyway.domain.config;

import kr.co.pennyway.domain.DomainRdbLocation;
import kr.co.pennyway.domain.common.importer.PennywayRdbDomainConfig;
import kr.co.pennyway.domain.common.repository.ExtendedRepositoryFactory;
import kr.co.pennyway.domain.domains.JpaPackageLocation;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing
@EntityScan(basePackageClasses = DomainRdbLocation.class)
@EnableJpaRepositories(basePackageClasses = JpaPackageLocation.class, repositoryFactoryBeanClass = ExtendedRepositoryFactory.class)
public class JpaConfig implements PennywayRdbDomainConfig {
}
