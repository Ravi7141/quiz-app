package com.example.quiz.config;

import com.example.quiz.entity.User;
import com.example.quiz.enums.Role;
import com.example.quiz.exception.ForbiddenException;
import com.example.quiz.exception.UnauthorizedException;
import com.example.quiz.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminSecurityInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Allow pre-flight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        User user = authService.getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("Authentication required. Please log in.");
        }

        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Access denied. Admin privileges required.");
        }

        return true;
    }
}
