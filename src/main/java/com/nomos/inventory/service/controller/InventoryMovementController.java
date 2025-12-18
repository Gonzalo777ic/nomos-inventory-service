package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.Product;
import com.nomos.inventory.service.model.InventoryItem; 
import com.nomos.inventory.service.model.InventoryMovement;
import com.nomos.inventory.service.repository.ProductRepository;
import com.nomos.inventory.service.repository.InventoryItemRepository; 
import com.nomos.inventory.service.repository.InventoryMovementRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

    @Autowired
    public InventoryMovementController(
            InventoryMovementRepository movementRepository,
            ProductRepository productRepository,
            InventoryItemRepository inventoryItemRepository) {
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
        this.inventoryItemRepository = inventoryItemRepository;
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
    public ResponseEntity<InventoryMovement> createMovement(@Valid @RequestBody InventoryMovement movement) {

        Long productId = movement.getProduct().getId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Producto con ID " + productId + " no existe")
                );
        movement.setProduct(product);

        if (movement.getInventoryItem() != null && movement.getInventoryItem().getId() != null) {
            Long itemId = movement.getInventoryItem().getId();
            InventoryItem item = inventoryItemRepository.findById(itemId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "InventoryItem con ID " + itemId + " no existe")
                    );
            movement.setInventoryItem(item);
        } else {
            movement.setInventoryItem(null); 
        }

        InventoryMovement createdMovement = movementRepository.save(movement);



        return new ResponseEntity<>(createdMovement, HttpStatus.CREATED);
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
