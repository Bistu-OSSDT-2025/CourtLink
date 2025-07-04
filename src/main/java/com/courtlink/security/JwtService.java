package com.courtlink.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
<<<<<<< HEAD
=======
import org.springframework.security.core.Authentication;
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

<<<<<<< HEAD
=======
    @Value("${jwt.token.clock-skew:5000}")
    private long clockSkew;

    @Value("${jwt.token.issuer:CourtLink}")
    private String issuer;

    @Value("${jwt.token.audience:CourtLink Users}")
    private String audience;

>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
<<<<<<< HEAD
        Map<String, Object> claims = new HashMap<>();
        // 添加角色信息到JWT claims中
        claims.put("roles", userDetails.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .collect(java.util.stream.Collectors.toList()));
        return generateToken(claims, userDetails);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
=======
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateExpiredToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, -1000);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
<<<<<<< HEAD
=======
                .setIssuer(issuer)
                .setAudience(audience)
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
<<<<<<< HEAD
        return extractExpiration(token).before(new Date());
=======
        return extractExpiration(token).before(new Date(System.currentTimeMillis() - clockSkew));
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
<<<<<<< HEAD
=======
                .setAllowedClockSkewSeconds(clockSkew / 1000)
                .requireIssuer(issuer)
                .requireAudience(audience)
>>>>>>> 3c5bc74901f039f3ddd32a6ae44b083d6266322e
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 