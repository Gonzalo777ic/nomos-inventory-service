package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    // Método para obtener una Orden y cargar sus detalles y proveedor en una sola consulta.
    @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.supplier s LEFT JOIN FETCH po.details d LEFT JOIN FETCH d.product p WHERE po.id = :id")
    Optional<PurchaseOrder> findByIdWithDetailsAndSupplier(Long id);

    // ⭐ CORRECCIÓN CLAVE: Cargar Supplier, Details Y el Product del detalle.
    // Usaremos esto en el GET /api/v1/purchase-orders
    @Query("SELECT po FROM PurchaseOrder po JOIN FETCH po.supplier s LEFT JOIN FETCH po.details d LEFT JOIN FETCH d.product p")
    List<PurchaseOrder> findAllWithSupplierAndDetailsAndProducts();
    // Nota: El JOIN FETCH de múltiples colecciones (d y p) puede generar un "cartesian product" (muchas filas),
    // pero es la forma estándar de resolver este problema de serialización para listados.
}