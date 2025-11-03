package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.InventoryItem;
import com.nomos.inventory.service.model.Product;
import com.nomos.inventory.service.repository.InventoryItemRepository;
import com.nomos.inventory.service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional; // 🎯 Importar la anotación Transactional
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/items") // 🎯 Nueva ruta base para el CRUD de existencias
@RequiredArgsConstructor
public class InventoryItemController {

    private final InventoryItemRepository itemRepository;
    private final ProductRepository productRepository;

    // 1. OBTENER EXISTENCIAS POR PRODUCTO (READ)
    // Ruta: GET /api/inventory/items/product/{productId}
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    @Transactional // <--- CORRECCIÓN: Permite que la relación LAZY (Product) se cargue durante la serialización.
    public ResponseEntity<List<InventoryItem>> getItemsByProduct(@PathVariable Long productId) {
        // Asegura que el producto exista (opcional, pero buena práctica)
        if (!productRepository.existsById(productId)) {
            return ResponseEntity.notFound().build();
        }
        // Utiliza el método derivado que creamos en el repositorio
        return ResponseEntity.ok(itemRepository.findByProductId(productId));
    }

    // 2. CREAR NUEVA EXISTENCIA / LOTE (CREATE)
    // Ruta: POST /api/inventory/items
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPPLIER')") // Solo quien introduce stock
    public ResponseEntity<InventoryItem> createItem(@RequestBody InventoryItem item) {
        // 🎯 VALIDACIÓN CLAVE: Asegurar que el ID del producto es válido
        if (item.getProduct() == null || item.getProduct().getId() == null) {
            return ResponseEntity.badRequest().body(null);
        }

        // Recuperar la entidad Product para asegurar la relación
        Product product = productRepository.findById(item.getProduct().getId())
                .orElse(null);

        if (product == null) {
            // El producto al que se intenta asociar el inventario no existe.
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Asociar la entidad Product completa al InventoryItem
        item.setProduct(product);

        InventoryItem savedItem = itemRepository.save(item);
        return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
    }

    // 3. MODIFICAR EXISTENCIA / LOTE (UPDATE)
    // Ruta: PUT /api/inventory/items/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Modificar existencias es crítico, solo Admin
    public ResponseEntity<InventoryItem> updateItem(@PathVariable Long id, @RequestBody InventoryItem itemDetails) {
        return itemRepository.findById(id).map(existingItem -> {

            // Actualizar los campos del lote
            existingItem.setCurrentStock(itemDetails.getCurrentStock());
            existingItem.setUnitCost(itemDetails.getUnitCost());
            existingItem.setLotNumber(itemDetails.getLotNumber());
            existingItem.setExpirationDate(itemDetails.getExpirationDate());
            existingItem.setLocation(itemDetails.getLocation());

            // 🛑 NOTA: No permitimos cambiar el product_id en la modificación del item.

            InventoryItem updatedItem = itemRepository.save(existingItem);
            return ResponseEntity.ok(updatedItem);
        }).orElse(ResponseEntity.notFound().build());
    }

    // 4. ELIMINAR EXISTENCIA / LOTE (DELETE)
    // Ruta: DELETE /api/inventory/items/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Eliminación solo para Admin
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (itemRepository.existsById(id)) {
            itemRepository.deleteById(id);
            // HTTP 204 No Content
            return ResponseEntity.noContent().build();
        } else {
            // HTTP 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }
}
