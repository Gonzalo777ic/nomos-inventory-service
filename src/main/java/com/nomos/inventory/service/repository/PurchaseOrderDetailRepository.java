package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.PurchaseOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad PurchaseOrderDetail.
 */
@Repository
public interface PurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetail, Long> {

    /**
     * Encuentra todos los detalles de una orden de compra específica.
     * @param purchaseOrderId ID de la Orden de Compra.
     * @return Lista de detalles asociados a esa orden.
     */
    List<PurchaseOrderDetail> findByPurchaseOrderId(Long purchaseOrderId);
}
