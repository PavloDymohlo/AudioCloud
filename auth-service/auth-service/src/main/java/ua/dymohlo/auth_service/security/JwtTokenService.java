package ua.dymohlo.auth_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.dymohlo.auth_service.entiti.User;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenService {
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(User user) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");

            return Jwts.builder()
                    .setSubject(user.getUserEmail())
                    .claim("id", user.getId())
                    .claim("role", user.getUserRole())
                    .claim("authorities", List.of("ROLE_" + user.getUserRole()))
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Error generating token", e);
        }
    }
}