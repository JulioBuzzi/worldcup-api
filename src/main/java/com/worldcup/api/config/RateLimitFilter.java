package com.worldcup.api.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter implements Filter {

    // Máximo de tentativas de login por IP em 15 minutos
    private static final int MAX_LOGIN_ATTEMPTS = 10;
    private static final long WINDOW_MS = 15 * 60 * 1000L;

    private final Map<String, AtomicInteger> attempts = new ConcurrentHashMap<>();
    private final Map<String, Long> windowStart = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Só limitar endpoints de auth
        String path = request.getRequestURI();
        if (!path.startsWith("/api/auth/")) {
            chain.doFilter(req, res);
            return;
        }

        String ip = getClientIp(request);
        long now = System.currentTimeMillis();

        // Reset janela se expirou
        windowStart.putIfAbsent(ip, now);
        if (now - windowStart.get(ip) > WINDOW_MS) {
            windowStart.put(ip, now);
            attempts.put(ip, new AtomicInteger(0));
        }

        AtomicInteger count = attempts.computeIfAbsent(ip, k -> new AtomicInteger(0));
        if (count.incrementAndGet() > MAX_LOGIN_ATTEMPTS) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Muitas tentativas. Aguarde 15 minutos.\",\"status\":429}");
            return;
        }

        chain.doFilter(req, res);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}