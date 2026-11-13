package com.valeerp.inventory.service.repository;

import com.valeerp.inventory.service.model.Alert;
import com.valeerp.inventory.service.model.AlertStatus;
import com.valeerp.inventory.service.model.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByStatusOrderBySeverityAscCreatedAtDesc(AlertStatus status);

    List<Alert> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<Alert> findByTypeAndStatus(AlertType type, AlertStatus status);

    boolean existsByProductIdAndTypeAndStatus(Long productId, AlertType type, AlertStatus status);

    boolean existsByInventoryItemIdAndTypeAndStatus(Long inventoryItemId, AlertType type, AlertStatus status);
}