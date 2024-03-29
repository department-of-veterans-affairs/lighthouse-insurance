package gov.va.api.lighthouse.insurance.service.config;

import gov.va.api.health.autoconfig.rest.PathRewriteFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PathRewriteConfig {
  @Bean
  FilterRegistrationBean<PathRewriteFilter> patientRegistrationFilter() {
    var registration = new FilterRegistrationBean<PathRewriteFilter>();
    PathRewriteFilter filter =
        PathRewriteFilter.builder()
            .removeLeadingPath("/insurance/")
            .removeLeadingPath("/services/fhir/v0/")
            .removeLeadingPath("/fhir/v0/")
            .build();
    registration.setFilter(filter);
    registration.addUrlPatterns(filter.removeLeadingPathsAsUrlPatterns());
    return registration;
  }
}
