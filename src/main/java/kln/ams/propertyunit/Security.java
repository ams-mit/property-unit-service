package kln.ams.propertyunit;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
class GatewayTokens {
    private static final Set<String> ROLES=Set.of("SYSTEM_ADMINISTRATOR","APARTMENT_MANAGER","OWNER","TENANT_RESIDENT","FINANCE_OFFICER","MAINTENANCE_COORDINATOR","TECHNICIAN","SERVICE_STAFF","SECURITY_OFFICER");
    private final String publicPem,privatePem;
    GatewayTokens(@Value("${gateway.jwt.public-key:}") String publicPem,@Value("${service.jwt.private-key:}") String privatePem) {
        this.publicPem=publicPem;this.privatePem=privatePem;
    }
    Claims verify(String token) {
        try {
            if(publicPem.isBlank()) throw new IllegalArgumentException("Missing Gateway public key");
            var jwt=Jwts.parserBuilder().setSigningKey(publicKey(publicPem)).build().parseClaimsJws(token);
            if(!"RS256".equals(jwt.getHeader().getAlgorithm()) || !"JWT".equals(jwt.getHeader().getType())) throw new IllegalArgumentException("Unsupported JWT header");
            Claims c=jwt.getBody();
            if(c.getSubject()==null || c.getIssuedAt()==null || c.getExpiration()==null || !c.getExpiration().after(c.getIssuedAt()) || c.getIssuedAt().after(new Date())) throw new IllegalArgumentException("Invalid claims");
            if("user".equals(c.get("type"))) {
                UUID.fromString(c.getSubject());
                Object roles=c.get("roles");
                if(!(roles instanceof List<?> list) || list.isEmpty() || list.stream().anyMatch(r->!(r instanceof String s) || !ROLES.contains(s))) throw new IllegalArgumentException("Invalid roles");
            } else if("service".equals(c.get("type"))) {
                if(c.containsKey("roles") || !c.getSubject().matches("[a-z]+(?:-[a-z]+)*-service")) throw new IllegalArgumentException("Invalid service");
            } else throw new IllegalArgumentException("Invalid token type");
            return c;
        } catch(Exception e) { throw new IllegalArgumentException("Invalid Gateway JWT",e); }
    }
    String serviceToken() {
        try {
            if(privatePem.isBlank()) throw new IllegalStateException("Service private key unavailable");
            Instant now=Instant.now();
            return Jwts.builder().setHeaderParam("typ","JWT").setSubject("property-unit-service").claim("type","service")
                    .setIssuedAt(Date.from(now)).setExpiration(Date.from(now.plusSeconds(300)))
                    .signWith(privateKey(privatePem),SignatureAlgorithm.RS256).compact();
        } catch(Exception e) { throw new IllegalStateException("Service signing unavailable",e); }
    }
    private static byte[] der(String pem) { return Base64.getDecoder().decode(pem.replaceAll("-----[^-]+-----","").replaceAll("\\s+","")); }
    private static PublicKey publicKey(String pem) throws Exception { return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der(pem))); }
    private static PrivateKey privateKey(String pem) throws Exception { return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der(pem))); }
}

@Component
class GatewayAuthentication extends OncePerRequestFilter {
    private final GatewayTokens tokens; private final SecurityErrors errors;
    GatewayAuthentication(GatewayTokens tokens,SecurityErrors errors) {this.tokens=tokens;this.errors=errors;}
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        String path=request.getRequestURI();
        if(!path.startsWith("/api/v1/")) { chain.doFilter(request,response);return; }
        String header=request.getHeader("Authorization");
        if(header==null || !header.startsWith("Bearer ") || header.length()<=7) {errors.write(response,HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Authentication required");return;}
        Claims c;
        try { c=tokens.verify(header.substring(7)); }
        catch(IllegalArgumentException e) { errors.write(response,HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Invalid authentication token");return; }
        try {
            boolean internal=path.startsWith("/api/v1/internal/");
            if(internal!= "service".equals(c.get("type"))) throw new IllegalArgumentException("Wrong token type");
            List<SimpleGrantedAuthority> authorities;
            if(internal) authorities=List.of(new SimpleGrantedAuthority("SERVICE_"+c.getSubject()));
            else authorities=((List<?>)c.get("roles")).stream().map(r->new SimpleGrantedAuthority("ROLE_"+r)).toList();
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(c.getSubject(),null,authorities));
        } catch(IllegalArgumentException e) {SecurityContextHolder.clearContext();errors.write(response,HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Invalid authentication token");return;}
        chain.doFilter(request,response);
    }
}

@Configuration
class SecurityConfiguration {
    private static final String[] UNIT_CALLERS={"SERVICE_resident-management-service","SERVICE_lease-occupancy-service","SERVICE_billing-payment-service","SERVICE_utility-charge-service","SERVICE_operations-service","SERVICE_community-service"};
    private static final String[] OWNERSHIP_CALLERS={"SERVICE_resident-management-service","SERVICE_lease-occupancy-service","SERVICE_billing-payment-service","SERVICE_operations-service","SERVICE_community-service"};
    @Bean SecurityFilterChain chain(HttpSecurity http,GatewayAuthentication authentication,SecurityErrors errors) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->errors.write(res,HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Authentication required"))
             .accessDeniedHandler((req,res,ex)->errors.write(res,HttpStatus.FORBIDDEN,"FORBIDDEN","Access denied")))
          .authorizeHttpRequests(a->a
             .requestMatchers("/actuator/health","/actuator/info","/swagger-ui.html","/swagger-ui/**","/v3/api-docs/**").permitAll()
             .requestMatchers(HttpMethod.GET,"/api/v1/internal/units/*/ownership").hasAnyAuthority(OWNERSHIP_CALLERS)
             .requestMatchers("/api/v1/internal/units/**").hasAnyAuthority(UNIT_CALLERS)
             .requestMatchers(HttpMethod.POST,"/api/v1/buildings","/api/v1/unit-types","/api/v1/units","/api/v1/ownerships").hasAnyAuthority("ROLE_SYSTEM_ADMINISTRATOR","ROLE_APARTMENT_MANAGER")
             .requestMatchers(HttpMethod.GET,"/api/v1/buildings").hasAnyAuthority("ROLE_SYSTEM_ADMINISTRATOR","ROLE_APARTMENT_MANAGER","ROLE_OWNER","ROLE_TENANT_RESIDENT")
             .requestMatchers(HttpMethod.GET,"/api/v1/unit-types","/api/v1/units","/api/v1/ownerships").authenticated()
             .anyRequest().denyAll())
          .addFilterBefore(authentication,UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
