package com.valeerp.inventory.stock;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockLotRepository extends JpaRepository<StockLot, Long> {

    Optional<StockLot> findByProductIdAndLotNumber(Long productId, String lotNumber);

    List<StockLot> findByProductIdOrderByLotNumberAsc(Long productId);

    boolean existsByProductIdAndLotNumber(Long productId, String lotNumber);
}
