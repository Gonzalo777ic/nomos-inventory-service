package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.Product;
import com.nomos.inventory.service.model.PurchaseOrder;
import com.nomos.inventory.service.model.PurchaseOrderDetail;
import com.nomos.inventory.service.repository.ProductRepository;
import com.nomos.inventory.service.repository.PurchaseOrderDetailRepository;
import com.nomos.inventory.service.repository.PurchaseOrderRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controlador REST para la gestión del detalle de Órdenes de Compra (PurchaseOrderDetail).
 * Base URL: /api/v1/purchase-order-details
 *
 * NOTA: La lógica de negocio se maneja directamente en el controlador
 * para seguir la arquitectura del proyecto existente (sin capa de Service).
 */
@RestController
@RequestMapping("/api/v1/purchase-order-details")
public class PurchaseOrderDetailController {

    private final PurchaseOrderDetailRepository detailRepository;
    private final PurchaseOrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Autowired
    public PurchaseOrderDetailController(
            PurchaseOrderDetailRepository detailRepository,
            PurchaseOrderRepository orderRepository,
            ProductRepository productRepository) {
        this.detailRepository = detailRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    /**
     * GET /api/v1/purchase-order-details : Obtener todos los detalles de órdenes.
     */
    @GetMapping
    public ResponseEntity<List<PurchaseOrderDetail>> getAllDetails() {
        List<PurchaseOrderDetail> details = detailRepository.findAll();
        return ResponseEntity.ok(details);
    }

    /**
     * GET /api/v1/purchase-order-details/by-order/{orderId} : Obtener detalles por ID de Orden.
     */
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<PurchaseOrderDetail>> getDetailsByOrderId(@PathVariable Long orderId) {

        if (!orderRepository.existsById(orderId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Orden de Compra con ID " + orderId + " no encontrada"
            );
        }
        List<PurchaseOrderDetail> details = detailRepository.findByPurchaseOrderId(orderId);
        return ResponseEntity.ok(details);
    }

    /**
     * POST /api/v1/purchase-order-details : Crear un nuevo detalle de orden.
     * Requiere que el PurchaseOrder y el Product asociados existan.
     */
    @PostMapping
    public ResponseEntity<PurchaseOrderDetail> createDetail(@Valid @RequestBody PurchaseOrderDetail detail) {

        Long orderId = detail.getPurchaseOrder().getId();
        PurchaseOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Orden de Compra con ID " + orderId + " no existe")
                );

        Long productId = detail.getProduct().getId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Producto con ID " + productId + " no existe")
                );

        detail.setPurchaseOrder(order);
        detail.setProduct(product);

        PurchaseOrderDetail createdDetail = detailRepository.save(detail);
        return new ResponseEntity<>(createdDetail, HttpStatus.CREATED);
    }

    /**
     * PUT /api/v1/purchase-order-details/{id} : Actualizar un detalle de orden.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrderDetail> updateDetail(@PathVariable Long id, @Valid @RequestBody PurchaseOrderDetail detailDetails) {
        PurchaseOrderDetail existingDetail = detailRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Detalle de Orden con ID " + id + " no encontrado para actualizar")
                );

        if (detailDetails.getPurchaseOrder() != null && !existingDetail.getPurchaseOrder().getId().equals(detailDetails.getPurchaseOrder().getId())) {
            Long newOrderId = detailDetails.getPurchaseOrder().getId();
            PurchaseOrder newOrder = orderRepository.findById(newOrderId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Nueva Orden de Compra con ID " + newOrderId + " no existe")
                    );
            existingDetail.setPurchaseOrder(newOrder);
        }

        if (detailDetails.getProduct() != null && !existingDetail.getProduct().getId().equals(detailDetails.getProduct().getId())) {
            Long newProductId = detailDetails.getProduct().getId();
            Product newProduct = productRepository.findById(newProductId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Nuevo Producto con ID " + newProductId + " no existe")
                    );
            existingDetail.setProduct(newProduct);
        }

        existingDetail.setQuantity(detailDetails.getQuantity());
        existingDetail.setUnitCost(detailDetails.getUnitCost());

        PurchaseOrderDetail updatedDetail = detailRepository.save(existingDetail);
        return ResponseEntity.ok(updatedDetail);
    }

    /**
     * DELETE /api/v1/purchase-order-details/{id} : Eliminar un detalle de orden.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDetail(@PathVariable Long id) {
        if (!detailRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Detalle de Orden con ID " + id + " no encontrado para eliminar"
            );
        }
        detailRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
