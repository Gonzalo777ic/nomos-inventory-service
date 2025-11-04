package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.ProductSupplier;
// Importamos correctamente la clase interna estática
import com.nomos.inventory.service.model.ProductSupplier.ProductSupplierId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositorio para la entidad de relación M:N ProductSupplier.
 * Utiliza ProductSupplierId como clave primaria compuesta.
 */
public interface ProductSupplierRepository extends JpaRepository<ProductSupplier, ProductSupplierId> {

    /**
     * Encuentra todos los proveedores asociados a un producto específico.
     * @param productId ID del producto.
     * @return Lista de relaciones ProductSupplier.
     */
    List<ProductSupplier> findByProductId(Long productId);

    /**
     * Encuentra el proveedor preferido para un producto específico.
     * @param productId ID del producto.
     * @param isPreferred Debe ser 'true'.
     * @return Relación ProductSupplier del proveedor preferido (puede ser nulo/opcional).
     */
    ProductSupplier findByProductIdAndIsPreferred(Long productId, Boolean isPreferred);
}
