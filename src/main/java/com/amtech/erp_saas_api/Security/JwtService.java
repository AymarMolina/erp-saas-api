package com.amtech.erp_saas_api.Security;

import com.amtech.erp_saas_api.Entity.Usuario;
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
    public String generarToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("usuario_id", usuario.getId());
        claims.put("empresa_id", usuario.getEmpresa().getId());
        claims.put("empresa_nombre", usuario.getEmpresa().getRazonSocial());
        claims.put("rol", usuario.getRol().getNombre());

        // 👇 AGREGAMOS LAS URLs DE LAS IMÁGENES AL TOKEN
        claims.put("foto_url", usuario.getFotoUrl());
        claims.put("logo_url", usuario.getEmpresa().getLogoUrl());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(usuario.getUsername() + ":" + usuario.getEmpresa().getId())
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