package com.fincore.fincore_api.security.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${fincore.security.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
            List<String> allowedOrigins
    ) {

        CorsConfiguration configuration = new CorsConfiguration();

        // Origens autorizadas.
        configuration.setAllowedOrigins(allowedOrigins);

        // Métodos HTTP permitidos.
        configuration.setAllowedMethods(List.of(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
        ));

        // Cabeçalhos aceitos.
        configuration.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "Accept"
        ));

        // Cabeçalhos que podem ser acessados pelo front-end.
        configuration.setExposedHeaders(List.of(
            "Location"
        ));

        // Não utilizaremos cookies de autenticação nesta arquitetura.
        configuration.setAllowCredentials(false);

        // Tempo de cache da resposta preflight.
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}