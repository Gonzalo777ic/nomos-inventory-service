package com.nomos.inventory.service.integration;

import com.nomos.inventory.service.model.dto.UserAuthDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class AuthClient {

    private static final Logger logger = LoggerFactory.getLogger(AuthClient.class);

    @Value("${auth.service.url:http://localhost:8080/api/auth/users}")
    private String authServiceUrl;

    private final RestTemplate restTemplate;

    public AuthClient() {
        this.restTemplate = new RestTemplate();
    }

    public Optional<Long> getSupplierIdByEmail(String email) {
        String url = authServiceUrl + "/info/" + email;

        logger.info(">>> [DEBUG AUTH] Intentando conectar a: {}", url);

        try {

            UserAuthDTO userDto = restTemplate.getForObject(url, UserAuthDTO.class);

            if (userDto == null) {
                logger.error(">>> [DEBUG AUTH] Respuesta NULL del servicio Auth. (¿Usuario no encontrado?)");
                return Optional.empty();
            }

            logger.info(">>> [DEBUG AUTH] ÉXITO. Usuario: {}, SupplierID: {}", userDto.getUsername(), userDto.getSupplierId());

            if (userDto.getSupplierId() == null) {
                logger.warn(">>> [DEBUG AUTH] El usuario existe pero NO tiene 'supplierId' asignado en la BD.");
            }

            return Optional.ofNullable(userDto.getSupplierId());

        } catch (HttpClientErrorException e) {

            logger.error(">>> [DEBUG AUTH] Error HTTP {}: {}", e.getStatusCode(), e.getStatusText());
            if (e.getStatusCode().value() == 404) {
                logger.error(">>> [DEBUG AUTH] CAUSA PROBABLE: El email '{}' no existe en la tabla 'users' del Auth-Service.", email);
            }
        } catch (Exception e) {

            logger.error(">>> [DEBUG AUTH] Error DE CONEXIÓN: {}", e.getMessage());
            logger.error(">>> [DEBUG AUTH] Verifica que 'nomos-auth-service' esté corriendo en el puerto 8080.");
        }

        return Optional.empty();
    }
}