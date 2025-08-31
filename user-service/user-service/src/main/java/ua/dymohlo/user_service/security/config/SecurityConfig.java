package ua.dymohlo.user_service.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ua.dymohlo.user_service.security.filter.InternalApiKeyFilter;
import ua.dymohlo.user_service.security.filter.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final InternalApiKeyFilter internalApiKeyFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/v1/users/saga").hasAuthority("INTERNAL_SERVICE")
                        .requestMatchers("/api/v1/users/saga/{userId}").hasAuthority("INTERNAL_SERVICE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/profile")
                        .hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/profile")
                        .hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/**")
                        .hasAnyRole("ADMIN","CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/by-email/**")
                        .hasAnyRole("ADMIN","CLIENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/by-email/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/by-subscription/**")
                        .hasAnyRole("ADMIN","CLIENT")
                        .anyRequest().authenticated()
                )

                .addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}