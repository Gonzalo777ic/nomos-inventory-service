package com.nomos.inventory.service.repository;

import com.nomos.inventory.service.model.Alert;
import com.nomos.inventory.service.model.AlertStatus;
import com.nomos.inventory.service.model.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {


}