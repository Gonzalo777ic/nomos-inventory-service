package com.valeerp.inventory.service.repository;

import com.valeerp.inventory.service.model.PriceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceRuleRepository extends JpaRepository<PriceRule, Long> {

    List<PriceRule> findByPriceListIdOrderByPriorityAscIdAsc(Long priceListId);
}
