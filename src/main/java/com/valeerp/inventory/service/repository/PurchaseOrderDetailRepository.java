package com.valeerp.inventory.service.repository;

import com.valeerp.inventory.service.model.PurchaseOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetail, Long> {

    
    List<PurchaseOrderDetail> findByPurchaseOrderId(Long purchaseOrderId);
}
