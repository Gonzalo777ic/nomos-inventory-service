package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.PurchaseOrder;
import com.nomos.inventory.service.model.Supplier;
import com.nomos.inventory.service.repository.PurchaseOrderRepository;
import com.nomos.inventory.service.repository.SupplierRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controlador REST para la gestión de Órdenes de Compra (PurchaseOrder).
 * Base URL: /api/v1/purchase-orders
 */
@RestController
@RequestMapping("/api/v1/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;

    @Autowired
    public PurchaseOrderController(PurchaseOrderRepository purchaseOrderRepository, SupplierRepository supplierRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
    }

    /**
     * GET /api/v1/purchase-orders : Obtener todas las órdenes de compra.
     */
    @GetMapping
    public ResponseEntity<List<PurchaseOrder>> getAllPurchaseOrders() {
        List<PurchaseOrder> orders = purchaseOrderRepository.findAll();
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/v1/purchase-orders/{id} : Obtener una orden de compra por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrder> getPurchaseOrderById(@PathVariable Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Orden de Compra con ID " + id + " no encontrada")
                );
        return ResponseEntity.ok(order);
    }

    /**
     * POST /api/v1/purchase-orders : Crear una nueva orden de compra.
     * Requiere que el Supplier asociado exista.
     */
    @PostMapping
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@Valid @RequestBody PurchaseOrder order) {
        // Validar que el proveedor (Supplier) exista antes de guardar la orden
        Long supplierId = order.getSupplier().getId();
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Proveedor con ID " + supplierId + " no existe")
                );

        order.setSupplier(supplier);
        PurchaseOrder createdOrder = purchaseOrderRepository.save(order);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    /**
     * PUT /api/v1/purchase-orders/{id} : Actualizar una orden de compra existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrder> updatePurchaseOrder(@PathVariable Long id, @Valid @RequestBody PurchaseOrder orderDetails) {
        PurchaseOrder existingOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Orden de Compra con ID " + id + " no encontrada para actualizar")
                );

        // 1. Validar y actualizar el Supplier si cambia
        if (orderDetails.getSupplier() != null && !existingOrder.getSupplier().getId().equals(orderDetails.getSupplier().getId())) {
            Long newSupplierId = orderDetails.getSupplier().getId();
            Supplier newSupplier = supplierRepository.findById(newSupplierId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Nuevo Proveedor con ID " + newSupplierId + " no existe")
                    );
            existingOrder.setSupplier(newSupplier);
        } else if (orderDetails.getSupplier() != null) {
            // Asegura que el objeto Supplier referenciado sea la instancia gestionada por JPA,
            // si el ID no ha cambiado pero el objeto Supplier fue pasado en el body.
            existingOrder.setSupplier(supplierRepository.findById(existingOrder.getSupplier().getId()).get());
        }


        // 2. Actualizar otros campos
        existingOrder.setOrderDate(orderDetails.getOrderDate());
        existingOrder.setDeliveryDate(orderDetails.getDeliveryDate());
        existingOrder.setTotalAmount(orderDetails.getTotalAmount());
        existingOrder.setStatus(orderDetails.getStatus());

        PurchaseOrder updatedOrder = purchaseOrderRepository.save(existingOrder);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * DELETE /api/v1/purchase-orders/{id} : Eliminar una orden de compra.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable Long id) {
        if (!purchaseOrderRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Orden de Compra con ID " + id + " no encontrada para eliminar"
            );
        }
        // NOTA: En un entorno real, se debería manejar la eliminación en cascada
        // o restricción con PurchaseOrderDetail.
        purchaseOrderRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
