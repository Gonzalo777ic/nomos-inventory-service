package com.nomos.inventory.service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración explícita para mapear la ruta URL '/images/**' a la ubicación física 'uploads/images/'.
 * Esto asegura que Spring Boot pueda servir los archivos estáticos desde el directorio externo
 * sin depender de la configuración implícita de application.properties.
 */
@Configuration
public class ResourceConfig implements WebMvcConfigurer {


    private static final String IMAGE_LOCATION = "file:./uploads/images/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/images/**")
                .addResourceLocations(IMAGE_LOCATION);
    }
}