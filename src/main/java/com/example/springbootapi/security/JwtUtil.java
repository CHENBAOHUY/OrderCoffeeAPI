package com.example.springbootapi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    private byte[] SECRET_KEY;

    @PostConstruct
    public void init() {
        String secret = System.getenv("JWT_SECRET_KEY");
        if (secret == null || secret.length() < 32) {
            System.out.println("⚠️ JWT_SECRET_KEY chưa được đặt hoặc quá ngắn. Sử dụng giá trị mặc định!");
            secret = "abcdefghijklmnopqrstuvwxyz1234567890123456";
        }
        SECRET_KEY = secret.getBytes(StandardCharsets.UTF_8);
    }

    // Sửa generateToken để nhận userId và role
    public String generateToken(String userId, String role) {
        if (SECRET_KEY == null || SECRET_KEY.length < 32) {
            throw new IllegalStateException("SECRET_KEY chưa được khởi tạo đúng cách.");
        }
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", "ROLE_" + role) // Thêm role với tiền tố ROLE_
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Thêm phương thức trích xuất role
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtException("Token đã hết hạn.", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtException("Token không được hỗ trợ.", e);
        } catch (MalformedJwtException e) {
            throw new JwtException("Token bị sai định dạng.", e);
        } catch (SignatureException e) {
            throw new JwtException("Chữ ký token không hợp lệ.", e);
        } catch (IllegalArgumentException e) {
            throw new JwtException("Token trống hoặc không hợp lệ.", e);
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String userId = extractUserId(token);
            return userId.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            System.err.println("Lỗi xác thực token: " + e.getMessage());
            return false;
        }
    }
}