package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.InventoryItem;
import com.nomos.inventory.service.model.Product;
import com.nomos.inventory.service.model.Warehouse;
import com.nomos.inventory.service.repository.InventoryItemRepository;
import com.nomos.inventory.service.repository.ProductRepository;
import com.nomos.inventory.service.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory/items")
@RequiredArgsConstructor
public class InventoryItemController {

    private final InventoryItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository; // Inyección para validar FK

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER', 'ROLE_AUDITOR')")
    public ResponseEntity<List<InventoryItem>> getItemsByProduct(@PathVariable Long productId) {
        if (!productRepository.existsById(productId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(itemRepository.findByProductId(productId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPPLIER')")
    public ResponseEntity<?> createItem(@Valid @RequestBody InventoryItem item) {

        Long productId = item.getProduct().getId();
        Long warehouseId = item.getWarehouse().getId();
        String lotNumber = item.getLotNumber();

        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<Warehouse> warehouseOpt = warehouseRepository.findById(warehouseId);

        if (productOpt.isEmpty() || warehouseOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("El Producto o el Almacén referenciado no existen.");
        }

        if (itemRepository.findByProductIdAndWarehouseIdAndLotNumber(productId, warehouseId, lotNumber).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un lote con número '" + lotNumber +
                            "' para el producto " + productId + " en el almacén " + warehouseId + ".");
        }

        item.setProduct(productOpt.get());
        item.setWarehouse(warehouseOpt.get());

        InventoryItem savedItem = itemRepository.save(item);
        return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @Valid @RequestBody InventoryItem itemDetails) {
        return itemRepository.findById(id).map(existingItem -> {

            Long newProductId = itemDetails.getProduct().getId();
            Long newWarehouseId = itemDetails.getWarehouse().getId();
            String newLotNumber = itemDetails.getLotNumber();

            Optional<Product> productOpt = productRepository.findById(newProductId);
            Optional<Warehouse> warehouseOpt = warehouseRepository.findById(newWarehouseId);

            if (productOpt.isEmpty() || warehouseOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("El Producto o el Almacén referenciado no existen.");
            }

            if (!newProductId.equals(existingItem.getProduct().getId()) ||
                    !newWarehouseId.equals(existingItem.getWarehouse().getId()) ||
                    !newLotNumber.equals(existingItem.getLotNumber())) {

                Optional<InventoryItem> conflict = itemRepository.findByProductIdAndWarehouseIdAndLotNumber(newProductId, newWarehouseId, newLotNumber);

                if (conflict.isPresent() && !conflict.get().getId().equals(id)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("El nuevo lote '" + newLotNumber +
                                    "' ya existe para el producto " + newProductId + " en el almacén " + newWarehouseId + ".");
                }
            }

            existingItem.setProduct(productOpt.get());
            existingItem.setWarehouse(warehouseOpt.get());
            existingItem.setCurrentStock(itemDetails.getCurrentStock());
            existingItem.setUnitCost(itemDetails.getUnitCost());
            existingItem.setLotNumber(newLotNumber);
            existingItem.setExpirationDate(itemDetails.getExpirationDate());
            existingItem.setLocation(itemDetails.getLocation());

            InventoryItem updatedItem = itemRepository.save(existingItem);
            return ResponseEntity.ok(updatedItem);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (itemRepository.existsById(id)) {
            itemRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/product/{productId}/total-stock")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER', 'ROLE_AUDITOR')")
    @Transactional(readOnly = true)
    public ResponseEntity<Integer> getTotalStockByProduct(@PathVariable Long productId) {
        if (!productRepository.existsById(productId)) {
            return ResponseEntity.notFound().build();
        }

        Integer totalStock = itemRepository.calculateTotalStockByProductId(productId);
        return ResponseEntity.ok(totalStock != null ? totalStock : 0);
    }
}
