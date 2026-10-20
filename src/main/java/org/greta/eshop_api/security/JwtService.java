package org.greta.eshop_api.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.greta.eshop_api.persistence.entities.CustomerEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    // jwt.secret : 32 octets aléatoires encodés en Base64, générés par `openssl rand -base64 32`
    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    public String generateToken(CustomerEntity customer) {
        Date now = new Date();
        return Jwts.builder()
                .subject(customer.getEmail())
                .claim("role", customer.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    // Vérifie la signature et l'expiration, puis renvoie l'email (le "subject")
    public String validateAndGetEmail(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            throw new JwtValidationException("Token expiré", e);
        } catch (MalformedJwtException e) {
            throw new JwtValidationException("Token mal formé", e);
        } catch (SignatureException e) {
            throw new JwtValidationException("Signature invalide", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtValidationException("Format JWT non supporté", e);
        } catch (IllegalArgumentException e) {
            throw new JwtValidationException("Token vide ou invalide", e);
        }
    }
}
