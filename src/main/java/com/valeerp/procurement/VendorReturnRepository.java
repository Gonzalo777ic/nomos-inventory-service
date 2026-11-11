package com.valeerp.procurement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VendorReturnRepository extends JpaRepository<VendorReturn, Long> {

    @Query("SELECT DISTINCT r FROM VendorReturn r LEFT JOIN FETCH r.lines WHERE r.id = :id")
    Optional<VendorReturn> findByIdWithLines(@Param("id") Long id);

    @Query("SELECT DISTINCT r FROM VendorReturn r LEFT JOIN FETCH r.lines ORDER BY r.returnDate DESC, r.id DESC")
    List<VendorReturn> findAllWithLines();

    List<VendorReturn> findByPurchaseOrderId(Long purchaseOrderId);

    List<VendorReturn> findByPurchaseOrderIdOrderByReturnDateDesc(Long purchaseOrderId);
}
