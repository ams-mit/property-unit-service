package kln.ams.propertyunit;

import static org.junit.jupiter.api.Assertions.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GatewayTokensTest {
    @Test void verifiesOnlyGatewaySignedCanonicalClaims() throws Exception {
        var generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);
        var gateway=generator.generateKeyPair();var other=generator.generateKeyPair();
        GatewayTokens tokens=new GatewayTokens(Base64.getEncoder().encodeToString(gateway.getPublic().getEncoded()),Base64.getEncoder().encodeToString(other.getPrivate().getEncoded()));
        Instant now=Instant.now();
        String valid=Jwts.builder().setHeaderParam("typ","JWT").setSubject(UUID.randomUUID().toString()).claim("type","user")
            .claim("roles",List.of("APARTMENT_MANAGER")).setIssuedAt(Date.from(now)).setExpiration(Date.from(now.plusSeconds(300)))
            .signWith(gateway.getPrivate(),SignatureAlgorithm.RS256).compact();
        assertEquals("user",tokens.verify(valid).get("type"));
        String wrongSignature=Jwts.builder().setHeaderParam("typ","JWT").setSubject(UUID.randomUUID().toString()).claim("type","user")
            .claim("roles",List.of("APARTMENT_MANAGER")).setIssuedAt(Date.from(now)).setExpiration(Date.from(now.plusSeconds(300)))
            .signWith(other.getPrivate(),SignatureAlgorithm.RS256).compact();
        assertThrows(IllegalArgumentException.class,()->tokens.verify(wrongSignature));
        String legacyRole=Jwts.builder().setHeaderParam("typ","JWT").setSubject(UUID.randomUUID().toString()).claim("type","user")
            .claim("roles",List.of("ADMIN")).setIssuedAt(Date.from(now)).setExpiration(Date.from(now.plusSeconds(300)))
            .signWith(gateway.getPrivate(),SignatureAlgorithm.RS256).compact();
        assertThrows(IllegalArgumentException.class,()->tokens.verify(legacyRole));
        assertThrows(IllegalArgumentException.class,()->tokens.verify(tokens.serviceToken()));
    }
}
