package com.nomos.inventory.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 🛑 CLAIM PERSONALIZADO DE AUTH0: Donde están nuestros roles.
    private static final String ROLES_CLAIM = "https://nomosstore.com/roles";

    /**
     * Define la cadena de filtros de seguridad para el servicio de Inventario.
     * Convierte el servicio en un Resource Server.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Configuración de CORS y CSRF (necesario en microservicios)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Establecer la política de sesión sin estado (STATELESS)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 🛑 RESTRICCIÓN GLOBAL POR ROL (Opción B)
                .authorizeHttpRequests(auth -> auth
                        // Únicamente los roles de Backend pueden acceder a /api/inventory/**
                        // Esto RECHAZA automáticamente a ROLE_CLIENT
                        .requestMatchers("/api/inventory/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_VENDOR", "ROLE_SUPPLIER")
                        // Permite acceso a todos los demás (ej: endpoints de salud, etc.)
                        .anyRequest().authenticated()
                )

                // 4. 🛑 CONFIGURAR COMO RESOURCE SERVER CON JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                // Aplicar el conversor personalizado para extraer los roles
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    /**
     * Crea un conversor personalizado para extraer los roles de los claims de Auth0.
     * El rol se extrae del claim "https://nomosstore.com/roles".
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // Define cómo extraer las autoridades (roles) del token
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Obtener el valor del claim personalizado (devuelve un List<String> o null)
            List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);

            if (roles == null) {
                return Collections.emptyList();
            }

            // Mapear cada rol a un SimpleGrantedAuthority (Spring Security espera este formato)
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority(role))
                    .collect(Collectors.toList());
        });
        return converter;
    }

    /**
     * Configuración básica de CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite orígenes de tu frontend (ej. localhost:3000)
        // 🔑 FIX CORS: Se incluye 'http://localhost:8081' para permitir la comunicación con el frontend.
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4000", "http://localhost:8081"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
