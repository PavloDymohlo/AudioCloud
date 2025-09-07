package ua.dymohlo.user_service.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class InternalApiKeyFilter extends OncePerRequestFilter {

    @Value("${saga.internal-api-key}")
    private String validApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        if (requestUri.contains("/saga")) {
            String apiKey = request.getHeader("X-Internal-API-Key");

            log.debug("Processing internal API request: URI={}, hasKey={}", requestUri, apiKey != null);

            if (validApiKey.equals(apiKey)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                "internal-service",
                                null,
                                List.of(new SimpleGrantedAuthority("INTERNAL_SERVICE"))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Internal API key validated successfully");
            } else {
                log.warn("Invalid or missing internal API key for saga endpoint: {}", requestUri);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\": \"Invalid or missing internal API key\"}");
                response.setContentType("application/json");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.contains("/saga");
    }
}