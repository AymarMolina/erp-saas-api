package com.amtech.erp_saas_api.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ── Rutas 100% Públicas ──
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/roles").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/empresas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/empresas").permitAll()
                        .requestMatchers("/api/v1/kardex", "/api/v1/kardex/**").permitAll()
                        // ── Endpoint Puente de SUNAT ──
                        .requestMatchers("/api/v1/sunat", "/api/v1/sunat/**").permitAll()

                        // ── Gestión Interna de la Empresa (ADMINISTRADOR) ──
                        .requestMatchers(HttpMethod.GET,   "/api/v1/empresas/{id}").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/empresas/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH,  "/api/v1/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET,    "/api/v1/usuarios", "/api/v1/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/v1/roles", "/api/v1/roles/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/v1/kardex/**").permitAll() // TEMPORAL: Para descartar errores

                        // ── Inventario, Almacenes y Compras ──
                        .requestMatchers("/api/v1/productos", "/api/v1/productos/**").hasAnyRole("ADMINISTRADOR", "ALMACENERO", "CAJERO", "EMBALADOR")
                        .requestMatchers("/api/v1/categorias", "/api/v1/categorias/**").hasAnyRole("ADMINISTRADOR", "ALMACENERO", "CAJERO", "EMBALADOR")
                        .requestMatchers("/api/v1/unidades-medida", "/api/v1/unidades-medida/**").hasAnyRole("ADMINISTRADOR", "ALMACENERO", "CAJERO", "EMBALADOR")
                        .requestMatchers("/api/v1/proveedores", "/api/v1/proveedores/**").hasAnyRole("ADMINISTRADOR", "ALMACENERO", "CAJERO", "EMBALADOR")
                        .requestMatchers("/api/v1/compras", "/api/v1/compras/**").hasAnyRole("ADMINISTRADOR", "ALMACENERO", "CAJERO", "EMBALADOR")
                        .requestMatchers("/api/v1/lotes", "/api/v1/lotes/**").hasAnyRole("ADMINISTRADOR", "ALMACENERO", "CAJERO", "EMBALADOR")

                        // ── Ventas, Clientes y Créditos ──
                        .requestMatchers("/api/v1/clientes", "/api/v1/clientes/**").hasAnyRole("ADMINISTRADOR", "ALMACENERO", "CAJERO", "EMBALADOR")
                        .requestMatchers("/api/v1/ventas", "/api/v1/ventas/**").hasAnyRole("ADMINISTRADOR", "ALMACENERO", "CAJERO", "EMBALADOR")
                        .requestMatchers("/api/v1/creditos", "/api/v1/creditos/**").hasAnyRole("ADMINISTRADOR", "ALMACENERO", "CAJERO", "EMBALADOR")

                        // ── Pedidos / Fulfillment ──
                        .requestMatchers("/api/v1/pedidos", "/api/v1/pedidos/**").hasAnyRole("ADMINISTRADOR", "ALMACENERO", "CAJERO", "EMBALADOR")

                        // ── Reportes ──
                        .requestMatchers("/api/v1/reportes", "/api/v1/reportes/**").hasAnyRole("ADMINISTRADOR", "ALMACENERO", "CAJERO", "EMBALADOR")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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