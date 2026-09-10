package lk.ac.kln.property_unit_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.gateway.public-key}")
    private String gatewayPublicKeyString;

    @Value("${jwt.service.private-key}")
    private String servicePrivateKeyString;

    @Value("${service.name}")
    private String serviceName;

    private PublicKey gatewayPublicKey;
    private PrivateKey servicePrivateKey;

    @PostConstruct
    public void init() throws Exception {
        this.gatewayPublicKey = loadPublicKey(gatewayPublicKeyString);
        this.servicePrivateKey = loadPrivateKey(servicePrivateKeyString);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(gatewayPublicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateServiceJwt() {
        // Standard specifies 5 minute expiration for service tokens
        long expirationTimeMs = 5 * 60 * 1000;
        
        return Jwts.builder()
                .setSubject(serviceName)
                .claim("type", "service")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeMs))
                .signWith(servicePrivateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    private PublicKey loadPublicKey(String key) throws Exception {
        if (key == null || key.trim().isEmpty()) {
            return null; // Return null so app can start, but it will fail on first request
        }
        
        // Standard java security APIs won't parse PEM headers
        key = key.replaceAll("-----[A-Z ]+-----", "").replaceAll("\\s+", "");
        
        if (key.isEmpty()) {
            return null;
        }

        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private PrivateKey loadPrivateKey(String key) throws Exception {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }

        key = key.replaceAll("-----[A-Z ]+-----", "").replaceAll("\\s+", "");

        if (key.isEmpty()) {
            return null;
        }

        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}
