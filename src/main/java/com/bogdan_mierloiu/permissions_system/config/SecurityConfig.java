package com.bogdan_mierloiu.permissions_system.config;

import com.bogdan_mierloiu.permissions_system.service.SignUpUser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;
import java.util.List;

import static com.bogdan_mierloiu.permissions_system.config.Endpoint.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SignUpUser signUpUser;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            UserPermissionsFilter userPermissionsFilter
    ) throws Exception {
        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(corsCustomizer -> corsCustomizer.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOriginPatterns(Collections.singletonList("*"));
                    config.setAllowedMethods(Collections.singletonList("*"));
                    config.setAllowCredentials(true);
                    config.setAllowedHeaders(Collections.singletonList("*"));
                    config.setExposedHeaders(List.of("Authorization"));
                    config.setMaxAge(1800L);
                    return config;
                }))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(permitAllEndpoints()).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2ResourceServerCustomizer ->
                        oauth2ResourceServerCustomizer.opaqueToken(opaqueTokenConfigurer ->
                                opaqueTokenConfigurer.introspector(tokenIntrospector())))
                .addFilterAfter(userPermissionsFilter, AuthorizationFilter.class);
        return http.build();
    }

    @Bean
    public OpaqueTokenIntrospector tokenIntrospector() {
        TokenIntrospector tokenIntrospector = new TokenIntrospector(signUpUser);
        tokenIntrospector.addIssuers(localAuthServerIssuer());
        return tokenIntrospector;
    }

    @Bean
    @ConfigurationProperties(prefix = "local.issuer")
    public TrustedIssuer localAuthServerIssuer() {
        return new TrustedIssuer();
    }

    public String[] permitAllEndpoints() {
        return new String[]{
                SWAGGER_UI.getUrl(),
                API_DOCS.getUrl(),
                ACTUATOR.getUrl(),
        };
    }
}
