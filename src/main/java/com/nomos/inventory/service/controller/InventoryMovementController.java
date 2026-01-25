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
    @Transactional // Importante: O se guardan ambos o ninguno
    public ResponseEntity<InventoryMovement> createMovement(@Valid @RequestBody InventoryMovement movement) {

        Long productId = movement.getProduct().getId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto no existe"));
        movement.setProduct(product);


        InventoryItem inventoryItem = null;

        if (movement.getInventoryItem() != null && movement.getInventoryItem().getId() != null) {
            inventoryItem = inventoryItemRepository.findById(movement.getInventoryItem().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item de inventario no encontrado"));
        } else {


            inventoryItem = inventoryItemRepository.findByProductId(productId)
                    .stream().findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay inventario inicializado para este producto"));
        }

        int currentStock = inventoryItem.getQuantity(); // Asumo que InventoryItem tiene un campo quantity
        int change = movement.getQuantityChange(); // Puede ser positivo (entrada) o negativo (salida)

        int newStock = currentStock + change;

        if (newStock < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente. Stock actual: " + currentStock);
        }

        inventoryItem.setQuantity(newStock);
        inventoryItemRepository.save(inventoryItem);

        movement.setInventoryItem(inventoryItem);
        movement.setBalanceAfter(newStock); // <--- GUARDAMOS EL SNAPSHOT



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
