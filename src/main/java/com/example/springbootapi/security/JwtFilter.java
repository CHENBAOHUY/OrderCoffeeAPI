package com.example.springbootapi.security;

import com.example.springbootapi.Service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // Bỏ qua /api/orders/callback
        String requestPath = request.getRequestURI();
        if (requestPath.equals("/api/orders/callback")) {
            chain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        String userId = null;

        // Kiểm tra và trích xuất token từ header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            try {
                userId = jwtUtil.extractUserId(token);
            } catch (RuntimeException e) {
                System.out.println("❌ Lỗi khi phân tích token: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token không hợp lệ hoặc đã hết hạn.");
                return;
            }
        } else {
            System.out.println("⚠️ Không tìm thấy token trong request hoặc định dạng sai.");
        }

        // Nếu có userId và chưa có thông tin xác thực trong SecurityContextHolder
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
            if (jwtUtil.validateToken(token, userDetails)) {
                String role = jwtUtil.extractRole(token); // Lấy role từ token
                System.out.println("✅ Token hợp lệ, xác thực người dùng: " + userId + ", role: " + role);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null,
                                Collections.singletonList(new SimpleGrantedAuthority(role)));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                System.out.println("❌ Token không hợp lệ.");
            }
        }

        chain.doFilter(request, response);
    }
}