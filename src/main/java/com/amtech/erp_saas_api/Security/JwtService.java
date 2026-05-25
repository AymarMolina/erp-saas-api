package com.amtech.erp_saas_api.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms:86400000}") // 24 horas por defecto
    private long expirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Genera un token JWT para el usuario autenticado.
     * Incluye empresa_id y rol como claims extra para evitar
     * consultas extra en cada request.
     */
    public String generarToken(Integer usuarioId, String username, Integer empresaId, String rol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("usuario_id", usuarioId);
        claims.put("empresa_id", empresaId);
        claims.put("rol", rol);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username + ":" + empresaId) // subject único por empresa
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extraerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extraerUsername(String token) {
        // El subject es "username:empresaId"
        return extraerClaims(token).getSubject().split(":")[0];
    }

    public Integer extraerEmpresaId(String token) {
        return extraerClaims(token).get("empresa_id", Integer.class);
    }

    public Integer extraerUsuarioId(String token) {
        return extraerClaims(token).get("usuario_id", Integer.class);
    }

    public String extraerRol(String token) {
        return extraerClaims(token).get("rol", String.class);
    }

    public boolean esTokenValido(String token) {
        try {
            return !extraerClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}