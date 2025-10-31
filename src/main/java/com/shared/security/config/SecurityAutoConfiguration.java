package com.shared.security.config;

import com.shared.config.SharedLibConfigurationProperties;
import com.shared.config.SecurityProperties;
import com.shared.security.JwtAuthenticationFilter;
import com.shared.security.JwtConfig;
import com.shared.security.client.TokenIntrospectionClient;
import com.shared.security.rbac.DynamicEndpointAuthorizationManager;
import com.shared.security.rbac.client.AuthorizationMatrixClient;
import com.shared.security.rbac.client.EndpointAuthorizationMetadataClient;
import com.shared.security.rbac.client.PolicyEvaluationClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Configuration("sharedSecurityAutoConfiguration")
@ConditionalOnClass(HttpSecurity.class)
@ConditionalOnProperty(prefix = "shared-lib.security", name = "enabled", havingValue = "true")
@EnableConfigurationProperties({SharedLibConfigurationProperties.class, JwtConfig.class})
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtConfig jwtConfig,
                                                           ObjectProvider<TokenIntrospectionClient> introspectionClientProvider) {
        return new JwtAuthenticationFilter(jwtConfig, introspectionClientProvider.getIfAvailable());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   SharedLibConfigurationProperties sharedLibProperties,
                                                   ObjectProvider<DynamicEndpointAuthorizationManager> dynamicManagerProvider) throws Exception {
        applyDynamicRbacDefaults(sharedLibProperties.getSecurity());
        String[] permittedPaths = sharedLibProperties.getSecurity().getPermittedPaths();
        boolean dynamicEnabled = sharedLibProperties.getSecurity().getDynamicRbac().isEnabled();

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(permittedPaths).permitAll();
                if (dynamicEnabled) {
                    DynamicEndpointAuthorizationManager manager = dynamicManagerProvider.getIfAvailable();
                    if (manager != null) {
                        auth.anyRequest().access(manager);
                    } else {
                        auth.anyRequest().authenticated();
                    }
                } else {
                    auth.anyRequest().authenticated();
                }
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "shared-lib.security.introspection", name = "enabled", havingValue = "true", matchIfMissing = true)
    public TokenIntrospectionClient tokenIntrospectionClient(RestTemplateBuilder restTemplateBuilder,
                                                             SharedLibConfigurationProperties properties) {
        var introspection = properties.getSecurity().getIntrospection();
        var builder = restTemplateBuilder;
        if (introspection.getConnectTimeout() != null) {
            builder = builder.setConnectTimeout(introspection.getConnectTimeout());
        }
        if (introspection.getReadTimeout() != null) {
            builder = builder.setReadTimeout(introspection.getReadTimeout());
        }
        return new TokenIntrospectionClient(builder.build(), introspection);
    }

    @Bean(name = "sharedLibDynamicRbacRestTemplate")
    @ConditionalOnProperty(prefix = "shared-lib.security.dynamic-rbac", name = "enabled", havingValue = "true")
    public RestTemplate sharedLibDynamicRbacRestTemplate(RestTemplateBuilder restTemplateBuilder,
                                                         SharedLibConfigurationProperties properties) {
        applyDynamicRbacDefaults(properties.getSecurity());
        var dynamic = properties.getSecurity().getDynamicRbac();
        var builder = restTemplateBuilder;
        if (dynamic.getConnectTimeout() != null) {
            builder = builder.setConnectTimeout(dynamic.getConnectTimeout());
        }
        if (dynamic.getReadTimeout() != null) {
            builder = builder.setReadTimeout(dynamic.getReadTimeout());
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "shared-lib.security.dynamic-rbac", name = "enabled", havingValue = "true")
    public AuthorizationMatrixClient authorizationMatrixClient(
        @Qualifier("sharedLibDynamicRbacRestTemplate") RestTemplate restTemplate,
        SharedLibConfigurationProperties properties) {
        return new AuthorizationMatrixClient(restTemplate, properties.getSecurity().getDynamicRbac());
    }

    @Bean
    @ConditionalOnProperty(prefix = "shared-lib.security.dynamic-rbac", name = "enabled", havingValue = "true")
    public EndpointAuthorizationMetadataClient endpointAuthorizationMetadataClient(
        @Qualifier("sharedLibDynamicRbacRestTemplate") RestTemplate restTemplate,
        SharedLibConfigurationProperties properties) {
        return new EndpointAuthorizationMetadataClient(restTemplate, properties.getSecurity().getDynamicRbac());
    }

    @Bean
    @ConditionalOnBean(name = "sharedLibDynamicRbacRestTemplate")
    @ConditionalOnProperty(prefix = "shared-lib.security.dynamic-rbac", name = "policy-evaluation-enabled", havingValue = "true", matchIfMissing = true)
    public PolicyEvaluationClient policyEvaluationClient(
        @Qualifier("sharedLibDynamicRbacRestTemplate") RestTemplate restTemplate,
        SharedLibConfigurationProperties properties) {
        return new PolicyEvaluationClient(restTemplate, properties.getSecurity().getDynamicRbac());
    }

    @Bean
    @ConditionalOnProperty(prefix = "shared-lib.security.dynamic-rbac", name = "enabled", havingValue = "true")
    public DynamicEndpointAuthorizationManager dynamicEndpointAuthorizationManager(
        AuthorizationMatrixClient matrixClient,
        EndpointAuthorizationMetadataClient metadataClient,
        ObjectProvider<PolicyEvaluationClient> policyEvaluationClientProvider,
        SharedLibConfigurationProperties properties) {
        return new DynamicEndpointAuthorizationManager(
            matrixClient,
            metadataClient,
            policyEvaluationClientProvider.getIfAvailable(),
            properties.getSecurity().getDynamicRbac()
        );
    }

    private void applyDynamicRbacDefaults(SecurityProperties securityProperties) {
        if (securityProperties == null) {
            return;
        }
        SecurityProperties.DynamicRbacProperties dynamic = securityProperties.getDynamicRbac();
        if (dynamic == null) {
            return;
        }
        if (!StringUtils.hasText(dynamic.getBaseUrl())) {
            String introspectionUrl = securityProperties.getIntrospection().getUrl();
            if (StringUtils.hasText(introspectionUrl)) {
                try {
                    URI uri = URI.create(introspectionUrl);
                    String base = uri.getScheme() + "://" + uri.getAuthority();
                    dynamic.setBaseUrl(base);
                } catch (IllegalArgumentException ignored) {
                    // leave base url unset if introspection URL is invalid or missing parts
                }
            }
        }
    }
}
