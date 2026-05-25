package com.amtech.erp_saas_api.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ── Rutas 100% públicas (onboarding de nuevos clientes SaaS) ──
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/empresas").permitAll()

                        // Crear el primer administrador de una empresa recién registrada
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios").permitAll()

                        // ── Gestión interna (requiere ser ADMINISTRADOR de la empresa) ──
                        .requestMatchers(HttpMethod.GET,    "/api/v1/empresas/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH,  "/api/v1/empresas/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH,  "/api/v1/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET,    "/api/v1/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/v1/roles/**").hasRole("ADMINISTRADOR")

                        // ── Inventario y compras (ADMINISTRADOR + ALMACENERO) ──
                        .requestMatchers("/api/v1/productos/**").hasAnyRole("ADMINISTRADOR", "ALMACENERO", "CAJERO")
                        .requestMatchers("/api/v1/categorias/**").hasAnyRole("ADMINISTRADOR", "ALMACENERO")
                        .requestMatchers("/api/v1/unidades-medida/**").hasAnyRole("ADMINISTRADOR", "ALMACENERO")
                        .requestMatchers("/api/v1/proveedores/**").hasAnyRole("ADMINISTRADOR", "ALMACENERO")
                        .requestMatchers("/api/v1/compras/**").hasAnyRole("ADMINISTRADOR", "ALMACENERO")
                        .requestMatchers("/api/v1/kardex/**").hasAnyRole("ADMINISTRADOR", "ALMACENERO")

                        // ── Ventas y clientes (CAJERO + ADMINISTRADOR) ──
                        .requestMatchers("/api/v1/clientes/**").hasAnyRole("ADMINISTRADOR", "CAJERO")
                        .requestMatchers("/api/v1/ventas/**").hasAnyRole("ADMINISTRADOR", "CAJERO")

                        // ── Pedidos / fulfillment ──
                        .requestMatchers("/api/v1/pedidos/**").hasAnyRole("ADMINISTRADOR", "CAJERO", "EMBALADOR")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}