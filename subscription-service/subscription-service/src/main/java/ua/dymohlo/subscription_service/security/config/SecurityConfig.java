package ua.dymohlo.subscription_service.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ua.dymohlo.subscription_service.security.filter.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/actuator/health").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/subscriptions").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/subscriptions/TRIAL").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/v1/subscriptions/**")
//                        .hasAnyRole("CLIENT", "ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/v1/subscriptions/**")
                                .hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/v1/subscriptions/**")
                                .hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/subscriptions/**")
                                .hasRole("ADMIN")
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}