package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.Product;
import com.nomos.inventory.service.model.InventoryItem; 
import com.nomos.inventory.service.model.InventoryMovement;
import com.nomos.inventory.service.repository.ProductRepository;
import com.nomos.inventory.service.repository.InventoryItemRepository; 
import com.nomos.inventory.service.repository.InventoryMovementRepository;
import com.valeerp.inventory.StockService;
import com.valeerp.kernel.DomainException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/**
 * Controlador REST para la gestión de Movimientos de Inventario (InventoryMovement).
 * Base URL: /api/v1/inventory-movements
 */
@RestController
@RequestMapping("/api/v1/inventory-movements")
public class InventoryMovementController {

    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final StockService stockService;

    @Autowired
    public InventoryMovementController(
            InventoryMovementRepository movementRepository,
            ProductRepository productRepository,
            InventoryItemRepository inventoryItemRepository,
            StockService stockService) {
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.stockService = stockService;
    }

    /**
     * GET /api/v1/inventory-movements : Obtener todos los movimientos de inventario.
     */
    @GetMapping
    public ResponseEntity<List<InventoryMovement>> getAllMovements() {
        List<InventoryMovement> movements = movementRepository.findAll();
        return ResponseEntity.ok(movements);
    }

    /**
     * GET /api/v1/inventory-movements/{id} : Obtener un movimiento por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InventoryMovement> getMovementById(@PathVariable Long id) {
        InventoryMovement movement = movementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Movimiento de Inventario con ID " + id + " no encontrado")
                );
        return ResponseEntity.ok(movement);
    }

    /**
     * POST /api/v1/inventory-movements : Registrar un nuevo movimiento.
     */
    @PostMapping
    @Transactional
    public ResponseEntity<InventoryMovement> createMovement(@Valid @RequestBody InventoryMovement movement) {
        if (movement.getInventoryItem() == null || movement.getInventoryItem().getId() == null) {
            throw new DomainException("El movimiento debe indicar el lote");
        }
        InventoryMovement created = stockService.adjustLot(
                movement.getInventoryItem().getId(),
                movement.getQuantityChange(),
                movement.getType(),
                movement.getReason(),
                movement.getReferenceId(),
                movement.getReferenceService()
        );
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * GET /api/v1/inventory-movements/product/{productId} : Obtener trazabilidad por producto.
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryMovement>> getMovementsByProduct(@PathVariable Long productId) {
        List<InventoryMovement> movements = movementRepository.findByProductIdOrderByMovementDateDesc(productId);
        return ResponseEntity.ok(movements);
    }
}
