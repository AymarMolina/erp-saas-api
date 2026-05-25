package com.amtech.erp_saas_api.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Si no hay token o no empieza con "Bearer ", continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            if (jwtService.esTokenValido(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                String username    = jwtService.extraerUsername(token);
                Integer empresaId  = jwtService.extraerEmpresaId(token);
                Integer usuarioId  = jwtService.extraerUsuarioId(token);
                String rol         = jwtService.extraerRol(token);

                // Principal personalizado que lleva empresaId y usuarioId para los controllers
                ErpUserDetails userDetails = new ErpUserDetails(usuarioId, username, empresaId, rol);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + rol))
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Token inválido/expirado: simplemente no autenticamos; Security bloqueará si el endpoint lo requiere
        }

        filterChain.doFilter(request, response);
    }
}