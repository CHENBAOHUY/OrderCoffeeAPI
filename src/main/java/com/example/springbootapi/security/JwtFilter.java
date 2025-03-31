package com.example.springbootapi.security;

import com.example.springbootapi.Service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    // ✅ Danh sách API bỏ qua xác thực JWT
    private static final List<String> PUBLIC_URLS = List.of(
            "/api/users/register",
            "/api/users/forgot-password",
            "/api/users/reset-password",
            "/api/users/verify-otp",
            "/api/users/login"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // ✅ Bỏ qua filter nếu API thuộc danh sách PUBLIC_URLS
        if (PUBLIC_URLS.stream().anyMatch(requestPath::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        String phone = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            try {
                phone = jwtUtil.extractPhone(token);
            } catch (RuntimeException e) {
                System.out.println("❌ Lỗi khi phân tích token: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token không hợp lệ hoặc đã hết hạn.");
                return;
            }
        } else {
            System.out.println("⚠️ Không tìm thấy token trong request hoặc định dạng sai.");
        }

        if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(phone);

                if (jwtUtil.validateToken(token, userDetails)) {
                    System.out.println("✅ Token hợp lệ, xác thực người dùng: " + phone);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    System.out.println("❌ Token không hợp lệ.");
                }
            } catch (Exception e) {
                System.out.println("❌ Lỗi xác thực JWT: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Không thể xác thực: Token không hợp lệ hoặc đã hết hạn.");
        }

        chain.doFilter(request, response);
    }
}
