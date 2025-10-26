package com.telconova.suportsuite.security;

import com.telconova.suportsuite.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    // Clave secreta para firmar el token.
    @Value("${jwt.secret:clave_secreta_por_defecto_y_muy_larga_para_telconova_supportsuite}")
    private String jwtSecret;

    // Tiempo de vida del token se establece por defecto a 15 minutos (900,000 ms)
    @Value("${jwt.expiration.minutes:15}")
    private long jwtExpirationInMinutes;

    public String generateToken(User user) {
        Date now = new Date();
        long expirationTimeMillis = jwtExpirationInMinutes * 60 * 1000;
        Date expiryDate = new Date(now.getTime() + expirationTimeMillis);

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles());
        claims.put("userId", user.getId());

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .addClaims(claims)
                // Usando el método obsoleto pero compatible con versiones antiguas
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                // ⭐️ CAMBIO: Usamos setSigningKey(String key)
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public long getRemainingTimeInMs(String token) {
        try {
            Claims claims = Jwts.parser()
                    // ⭐️ CAMBIO: Usamos setSigningKey(String key)
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            long remaining = expiration.getTime() - new Date().getTime();
            return Math.max(0, remaining);

        } catch (ExpiredJwtException ex) {
            return 0;
        } catch (Exception ex) {
            return -1;
        }
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    // ⭐️ CAMBIO: Usamos setSigningKey(String key)
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            // Log: Firma JWT inválida
        } catch (ExpiredJwtException ex) {
            // Log: Token JWT expirado
        } catch (Exception ex) {
            // Log: Otro error de JWT
        }
        return false;
    }

    public String getJwtFromRequest(jakarta.servlet.http.HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}