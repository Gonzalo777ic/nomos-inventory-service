package com.valeerp.inventory.service.repository;

import com.valeerp.inventory.service.model.PriceList;
import com.valeerp.catalog.PriceListKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PriceListRepository extends JpaRepository<PriceList, Long> {

    Optional<PriceList> findByCode(String code);

    boolean existsByCode(String code);

    Optional<PriceList> findFirstByKindOrderByIdAsc(PriceListKind kind);
}
