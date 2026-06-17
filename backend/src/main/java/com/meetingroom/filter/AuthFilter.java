package com.meetingroom.filter;

import com.meetingroom.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

@WebFilter("/api/*")
public class AuthFilter implements Filter {

    // Fully public — no auth needed
    private static final Set<String> PUBLIC_PATHS = Set.of(
        "/api/auth/register",
        "/api/auth/login"
    );

    // Public for GET only (browsing rooms)
    private static final Set<String> PUBLIC_GET_PATHS = Set.of(
        "/api/rooms"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        String path = httpReq.getRequestURI();
        String method = httpReq.getMethod();

        // Allow OPTIONS preflight always
        if ("OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        // Fully public paths
        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::endsWith);
        if (isPublic) {
            chain.doFilter(request, response);
            return;
        }

        // GET /api/rooms and /api/rooms/{id} are public for browsing
        boolean isPublicGet = "GET".equalsIgnoreCase(method) &&
            PUBLIC_GET_PATHS.stream().anyMatch(p -> path.endsWith(p) || path.contains(p + "/"));
        if (isPublicGet) {
            chain.doFilter(request, response);
            return;
        }

        // All other paths require authentication
        User user = (User) httpReq.getSession(false) != null
            ? (User) httpReq.getSession(false).getAttribute("user")
            : null;

        if (user == null) {
            httpRes.setContentType("application/json");
            httpRes.setCharacterEncoding("UTF-8");
            httpRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpRes.getWriter().write("{\"success\":false,\"message\":\"Authentication required\"}");
            return;
        }

        // Admin-only paths
        if (path.contains("/api/admin/") ||
            ("POST".equals(method) && path.matches(".*/api/rooms$")) ||
            ("PUT".equals(method) && path.matches(".*/api/rooms/\\d+$")) ||
            ("DELETE".equals(method) && path.matches(".*/api/rooms/\\d+$")) ||
            path.matches(".*/api/reservations/\\d+/approve$") ||
            path.matches(".*/api/reservations/\\d+/reject$") ||
            (path.contains("/equipment") && (method.equals("POST") || method.equals("PUT") || method.equals("DELETE")))) {

            if (!"admin".equals(user.getRole())) {
                httpRes.setContentType("application/json");
                httpRes.setCharacterEncoding("UTF-8");
                httpRes.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpRes.getWriter().write("{\"success\":false,\"message\":\"Admin access required\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
