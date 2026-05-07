package com.superdata.security.basic;

import com.superdata.entity.ApiClient;
import com.superdata.mapper.ApiClientMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

public class BasicAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(BasicAuthFilter.class);

    private final ApiClientMapper apiClientMapper;
    private final PasswordEncoder passwordEncoder;

    public BasicAuthFilter(ApiClientMapper apiClientMapper, PasswordEncoder passwordEncoder) {
        this.apiClientMapper = apiClientMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (!path.startsWith("/open-api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String[] credentials = getBasicAuthCredentials(request);

            if (credentials != null) {
                String clientId = credentials[0];
                String clientSecret = credentials[1];

                ApiClient apiClient = apiClientMapper.selectByClientId(clientId);

                if (apiClient != null && passwordEncoder.matches(clientSecret, apiClient.getClientSecret())) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    clientId,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_CLIENT"))
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Basic auth successful for client: {}", clientId);
                } else {
                    logger.warn("Invalid Basic auth credentials for client: {}", clientId);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"code\":401,\"message\":\"认证失败：无效的客户端凭证\"}");
                    return;
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setHeader("WWW-Authenticate", "Basic realm=\"Super Data API\"");
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"code\":401,\"message\":\"认证失败：缺少Basic认证头\"}");
                return;
            }
        } catch (Exception e) {
            logger.error("Basic auth error: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"code\":401,\"message\":\"认证失败\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String[] getBasicAuthCredentials(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Basic ")) {
            String base64Credentials = authHeader.substring(6);
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(decodedBytes, StandardCharsets.UTF_8);

            int colonIndex = credentials.indexOf(':');
            if (colonIndex > 0) {
                return new String[]{
                        credentials.substring(0, colonIndex),
                        credentials.substring(colonIndex + 1)
                };
            }
        }
        return null;
    }
}
