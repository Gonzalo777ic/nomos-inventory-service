package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.supplier s LEFT JOIN FETCH po.details d LEFT JOIN FETCH d.product p WHERE po.id = :id")
    Optional<PurchaseOrder> findByIdWithDetailsAndSupplier(Long id);


    @Query("SELECT po FROM PurchaseOrder po JOIN FETCH po.supplier s LEFT JOIN FETCH po.details d LEFT JOIN FETCH d.product p")
    List<PurchaseOrder> findAllWithSupplierAndDetailsAndProducts();


}