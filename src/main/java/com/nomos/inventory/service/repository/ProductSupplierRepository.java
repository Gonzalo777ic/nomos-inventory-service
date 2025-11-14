package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.ProductSupplier;
import com.nomos.inventory.service.model.ProductSupplier.ProductSupplierId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad de relación M:N ProductSupplier.
 * Utiliza ProductSupplierId como clave primaria compuesta (@IdClass).
 */
public interface ProductSupplierRepository extends JpaRepository<ProductSupplier, ProductSupplierId> {

    /**
     * Encuentra todos los proveedores asociados a un producto específico.
     * @param productId ID del producto.
     * @return Lista de relaciones ProductSupplier.
     */
    List<ProductSupplier> findByProductId(Long productId);

    /**
     * Encuentra TODOS los proveedores marcados como preferidos para un producto específico.
     * Esto devuelve una lista, ya que es el método utilizado para corregir la inconsistencia
     * si hay más de uno marcado como true, o si se quiere ver el único.
     * @param productId ID del producto.
     * @param isPreferred Debe ser 'true'.
     * @return Lista de relaciones ProductSupplier.
     */
    // Se cambia a List<> para que el Controller pueda manejar la lógica de desactivación
    List<ProductSupplier> findByProductIdAndIsPreferred(Long productId, Boolean isPreferred);
}