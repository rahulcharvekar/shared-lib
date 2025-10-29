package com.shared.security.config;

import com.shared.config.SharedLibConfigurationProperties;
import com.shared.security.JwtAuthenticationFilter;
import com.shared.security.JwtConfig;
import com.shared.security.client.TokenIntrospectionClient;
import org.springframework.beans.factory.ObjectProvider;
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
                                                   SharedLibConfigurationProperties sharedLibProperties) throws Exception {
        String[] permittedPaths = sharedLibProperties.getSecurity().getPermittedPaths();
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(permittedPaths).permitAll()
                .anyRequest().authenticated()
            )
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
}
