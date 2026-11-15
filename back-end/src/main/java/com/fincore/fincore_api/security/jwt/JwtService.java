package com.fincore.fincore_api.security.jwt;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long accessTokenTtlSeconds;

    public JwtService(
        JwtEncoder jwtEncoder,
        @Value("${fincore.security.jwt.issuer}") String issuer,
        @Value("${fincore.security.jwt.access-token-ttl-seconds}") long accessTokenTtlSeconds
    ){
        if (accessTokenTtlSeconds <= 0){
            throw new IllegalArgumentException(
                "A validade do access token precisa ser positiva."
            );
        }

        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    public String generateAccessToken(Authentication authentication){

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(accessTokenTtlSeconds);

        List<String> authorities = authentication.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(issuer)
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .subject(authentication.getName())
            .claim("roles", authorities)
            .build();

        JwsHeader header = JwsHeader
            .with(MacAlgorithm.HS256)
            .build();

        return jwtEncoder.encode(
            JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlSeconds;
    }
}
