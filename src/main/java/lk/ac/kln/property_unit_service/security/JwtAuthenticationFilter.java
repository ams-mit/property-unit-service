package lk.ac.kln.property_unit_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            Claims claims = jwtService.extractAllClaims(jwt);
            
            // Expected claims based on the standard
            String type = claims.get("type", String.class);
            String sub = claims.getSubject();

            if (type == null || sub == null) {
                throw new JwtException("Missing required claims: type or sub");
            }

            List<GrantedAuthority> authorities = new ArrayList<>();

            if ("user".equals(type)) {
                List<?> roles = claims.get("roles", List.class);
                if (roles != null) {
                    for (Object role : roles) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toString()));
                    }
                }
            } else if ("service".equals(type)) {
                // Add a specific authority for the calling service
                authorities.add(new SimpleGrantedAuthority("ROLE_SERVICE_" + sub));
            } else {
                throw new JwtException("Invalid token type");
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        sub,
                        null,
                        authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (ExpiredJwtException e) {
            request.setAttribute("exception", "expired");
        } catch (JwtException | IllegalArgumentException e) {
            request.setAttribute("exception", "invalid");
        }

        filterChain.doFilter(request, response);
    }
}
