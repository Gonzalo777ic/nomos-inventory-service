package com.valeerp.inventory.service.repository;

import com.valeerp.inventory.service.model.PriceListItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceListItemRepository extends JpaRepository<PriceListItem, Long> {

    List<PriceListItem> findByPriceListIdOrderByProductIdAsc(Long priceListId);

    Optional<PriceListItem> findByPriceListIdAndProductIdAndVariantId(Long priceListId, Long productId, Long variantId);

    Optional<PriceListItem> findByPriceListIdAndProductIdAndVariantIdIsNull(Long priceListId, Long productId);
}
