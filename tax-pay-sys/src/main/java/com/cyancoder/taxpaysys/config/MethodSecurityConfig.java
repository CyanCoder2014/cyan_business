package com.cyancoder.taxpaysys.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Spring Security 6 replaced {@code @EnableGlobalMethodSecurity} and
 * {@code GlobalMethodSecurityConfiguration} with {@code @EnableMethodSecurity},
 * and removed the advisor the old machinery was built on. The previous
 * annotation still compiled against the current classpath but blew up at
 * startup with NoClassDefFoundError for MethodSecurityMetadataSourceAdvisor,
 * so this context could never load.
 *
 * <p>prePostEnabled defaults to true; securedEnabled and jsr250Enabled are
 * carried over explicitly to preserve @Secured and @RolesAllowed support.
 */
@Configuration
@EnableMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true)
public class MethodSecurityConfig {
}
