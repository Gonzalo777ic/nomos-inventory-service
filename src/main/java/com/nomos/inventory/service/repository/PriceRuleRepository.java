package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.PriceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceRuleRepository extends JpaRepository<PriceRule, Long> {

    List<PriceRule> findByPriceListIdOrderByPriorityAscIdAsc(Long priceListId);
}
