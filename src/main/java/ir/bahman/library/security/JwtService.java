package ir.bahman.library.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import ir.bahman.library.util.KeyUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    private final long accessTokenSeconds;
    private final long refreshTokenSeconds;
    private final String issuer;

    public JwtService(
            @Value("${jwt.access-token-seconds:900}") long accessTokenSeconds,
            @Value("${jwt.refresh-token-seconds:1209600}") long refreshTokenSeconds,
            @Value("${jwt.issuer:library}") String issuer
    ) throws Exception {
        this.privateKey = KeyUtils.loadPrivateKey("keys/private_key.pem");
        this.publicKey = KeyUtils.loadPublicKey("keys/public_key.pem");
        this.accessTokenSeconds = accessTokenSeconds;
        this.refreshTokenSeconds = refreshTokenSeconds;
        this.issuer = issuer;
    }

    public String generateAccessToken(UUID uuid) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(uuid.toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenSeconds)))
                .claims(Map.of("type", "access"))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String generateRefreshToken(UUID uuid) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(uuid.toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTokenSeconds)))
                .claim("type", "refresh")
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public boolean isTokenValid(String token, String expectedType) {
        try {
            Jws<Claims> jws = Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token);
            Claims claims = jws.getPayload();
            return claims.getExpiration().after(new Date()) &&
                    expectedType.equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public String extractAuthId(String token) {
        return Jwts.parser().verifyWith(publicKey).build()
                .parseSignedClaims(token)
                .getPayload().getSubject();
    }
}