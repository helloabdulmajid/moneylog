package in.abdulmajid.moneylog.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";
    public static final String TOKEN_TYPE_ADMIN_ACCESS = "admin_access";
    public static final String TOKEN_TYPE_ADMIN_REFRESH = "admin_refresh";
    public static final String CLAIM_TOKEN_TYPE = "type";
    public static final String CLAIM_SESSION_ID = "sid";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${admin.access-token-expiration:1800000}")
    private long adminAccessTokenExpiration;

    @Value("${admin.refresh-token-expiration:604800000}")
    private long adminRefreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateAccessToken(UserDetails userDetails, String sessionId) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                .claim(CLAIM_SESSION_ID, sessionId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails, String sessionId) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
                .claim(CLAIM_SESSION_ID, sessionId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateAdminAccessToken(String email, String sessionId) {
        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ADMIN_ACCESS)
                .claim(CLAIM_SESSION_ID, sessionId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + adminAccessTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateAdminRefreshToken(String email, String sessionId) {
        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ADMIN_REFRESH)
                .claim(CLAIM_SESSION_ID, sessionId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + adminRefreshTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isAdminAccessToken(String token) {
        return TOKEN_TYPE_ADMIN_ACCESS.equals(extractClaim(token, CLAIM_TOKEN_TYPE));
    }

    public boolean isAdminRefreshToken(String token) {
        return TOKEN_TYPE_ADMIN_REFRESH.equals(extractClaim(token, CLAIM_TOKEN_TYPE));
    }

    public String extractClaim(String token, String claimName) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Object value = claims.get(claimName);
            return value == null ? null : value.toString();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(extractClaim(token, CLAIM_TOKEN_TYPE));
    }

    public boolean isAccessToken(String token) {
        return TOKEN_TYPE_ACCESS.equals(extractClaim(token, CLAIM_TOKEN_TYPE));
    }

    public String extractSessionId(String token) {
        return extractClaim(token, CLAIM_SESSION_ID);
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }
}
