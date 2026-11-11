package com.valeerp.inventory.stock;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockSerialRepository extends JpaRepository<StockSerial, Long> {

    Optional<StockSerial> findByProductIdAndSerialNumber(Long productId, String serialNumber);

    List<StockSerial> findByProductIdOrderBySerialNumberAsc(Long productId);

    boolean existsByProductIdAndSerialNumber(Long productId, String serialNumber);
}
