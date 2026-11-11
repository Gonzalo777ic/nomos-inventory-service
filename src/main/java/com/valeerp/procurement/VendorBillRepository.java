package com.valeerp.procurement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VendorBillRepository extends JpaRepository<VendorBill, Long> {

    @Query("SELECT DISTINCT b FROM VendorBill b LEFT JOIN FETCH b.lines WHERE b.id = :id")
    Optional<VendorBill> findByIdWithLines(@Param("id") Long id);

    @Query("SELECT DISTINCT b FROM VendorBill b LEFT JOIN FETCH b.lines ORDER BY b.billDate DESC, b.id DESC")
    List<VendorBill> findAllWithLines();

    List<VendorBill> findBySupplierId(Long supplierId);

    List<VendorBill> findByPurchaseOrderId(Long purchaseOrderId);
}
