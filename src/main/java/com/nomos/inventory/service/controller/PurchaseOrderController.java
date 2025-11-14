package com.nomos.inventory.service.controller;
import org.springframework.transaction.annotation.Transactional; // Importar Transactional
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
    @Transactional(readOnly = true)
    public ResponseEntity<List<PurchaseOrder>> getAllPurchaseOrders() {
        // ⭐ USAR EL MÉTODO CORREGIDO
        List<PurchaseOrder> orders = purchaseOrderRepository.findAllWithSupplierAndDetailsAndProducts();
        return ResponseEntity.ok(orders);
    }
    /**
     * GET /api/v1/purchase-orders/{id} : Obtener una orden de compra por su ID.
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<PurchaseOrder> getPurchaseOrderById(@PathVariable Long id) {
        // ⭐ CORRECCIÓN CLAVE: Usar el método con FETCH JOIN para cargar Supplier y Details.
        PurchaseOrder order = purchaseOrderRepository.findByIdWithDetailsAndSupplier(id)
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
    @Transactional // Añadir @Transactional aquí también para asegurar el guardado de detalles
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@Valid @RequestBody PurchaseOrder order) {
        // Validar que el proveedor (Supplier) exista antes de guardar la orden
        Long supplierId = order.getSupplier().getId();
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Proveedor con ID " + supplierId + " no existe")
                );

        order.setSupplier(supplier);

        // ⭐ CORRECCIÓN: Asegurar la referencia bidireccional antes de guardar
        if (order.getDetails() != null) {
            order.getDetails().forEach(detail -> detail.setPurchaseOrder(order));
        }

        PurchaseOrder createdOrder = purchaseOrderRepository.save(order);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    /**
     * PUT /api/v1/purchase-orders/{id} : Actualizar una orden de compra existente.
     */
    @PutMapping("/{id}")
    @Transactional // IMPORTANTE: Necesitas @Transactional para que el EntityManager maneje la sincronización
    public ResponseEntity<PurchaseOrder> updatePurchaseOrder(@PathVariable Long id, @Valid @RequestBody PurchaseOrder orderDetails) {
        PurchaseOrder existingOrder = purchaseOrderRepository.findByIdWithDetailsAndSupplier(id) // Usar el fetch-join
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Orden de Compra con ID " + id + " no encontrada para actualizar")
                );

        // 1. Validar y actualizar el Supplier
        if (orderDetails.getSupplier() != null && !existingOrder.getSupplier().getId().equals(orderDetails.getSupplier().getId())) {
            Long newSupplierId = orderDetails.getSupplier().getId();
            Supplier newSupplier = supplierRepository.findById(newSupplierId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Nuevo Proveedor con ID " + newSupplierId + " no existe")
                    );
            existingOrder.setSupplier(newSupplier);
        }
        // Si el proveedor no cambia, no necesitamos hacer nada más aquí, ya que 'existingOrder'
        // ya tiene su Supplier gestionado (o si el frontend lo envía, JPA lo ignorará si el ID es el mismo)

        // 2. Actualizar campos de la cabecera
        existingOrder.setOrderDate(orderDetails.getOrderDate());
        existingOrder.setDeliveryDate(orderDetails.getDeliveryDate());
        existingOrder.setTotalAmount(orderDetails.getTotalAmount());
        existingOrder.setStatus(orderDetails.getStatus());

        // ⭐ 3. LÓGICA CRÍTICA DE ACTUALIZACIÓN DE DETALLES (Sincronización)

        // a) Limpiar la lista existente (CASCADE y ORPHAN REMOVAL eliminarán los detalles viejos en DB)
        // Esto solo funciona si `orphanRemoval = true` está en la entidad JPA.
        existingOrder.getDetails().clear();

        // b) Añadir los nuevos detalles y configurar la referencia bidireccional
        if (orderDetails.getDetails() != null) {
            orderDetails.getDetails().forEach(newDetail -> {
                newDetail.setPurchaseOrder(existingOrder); // Establece la referencia al padre
                existingOrder.getDetails().add(newDetail); // Añade al padre
            });
        }

        // 4. Guardar (JPA detectará los cambios, eliminará los viejos, insertará los nuevos)
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
