package com.nomos.inventory.service.service;

import com.nomos.inventory.service.model.Alert;
import com.nomos.inventory.service.model.AlertStatus;
import com.nomos.inventory.service.model.dto.StockAlertDTO;
import com.nomos.inventory.service.repository.AlertRepository;
import com.nomos.inventory.service.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final ProductRepository productRepository;
    private final AlertRepository alertRepository;


}