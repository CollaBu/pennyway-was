package kr.co.pennyway.domain.config;

import kr.co.pennyway.domain.common.importer.EnablePennywayRedisDomainConfig;
import kr.co.pennyway.domain.common.importer.PennywayDomainConfig;
import kr.co.pennyway.domain.common.importer.PennywayRedisDomainConfigGroup;

@EnablePennywayRedisDomainConfig(value = PennywayRedisDomainConfigGroup.LETTUCE)
public class LettuceConfig implements PennywayDomainConfig {
}
