package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.ProductAttributeValue;
import com.nomos.inventory.service.model.ProductAttributeValue.ProductAttributeValueId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositorio para la entidad ProductAttributeValue.
 * Utiliza ProductAttributeValueId como clave primaria compuesta.
 */
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, ProductAttributeValueId> {

    /**
     * Encuentra todos los valores de atributos asociados a un producto específico.
     * @param productId ID del producto.
     * @return Lista de relaciones ProductAttributeValue.
     */
    List<ProductAttributeValue> findByProductId(Long productId);

    /**
     * Encuentra el valor de un atributo específico para un producto.
     * @param productId ID del producto.
     * @param attributeId ID del atributo.
     * @return El ProductAttributeValue si existe, o null.
     */
    List<ProductAttributeValue> findByProductIdAndAttributeId(Long productId, Long attributeId);
}
