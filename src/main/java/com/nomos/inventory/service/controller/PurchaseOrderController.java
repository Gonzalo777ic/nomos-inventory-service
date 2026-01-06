package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.integration.AuthClient;
import com.nomos.inventory.service.model.OrderStatus;
import com.nomos.inventory.service.model.PurchaseOrder;
import com.nomos.inventory.service.model.Supplier;
import com.nomos.inventory.service.repository.PurchaseOrderRepository;
import com.nomos.inventory.service.repository.SupplierRepository;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/purchase-orders")
public class PurchaseOrderController {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderController.class);

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final AuthClient authClient;

    @Autowired
    public PurchaseOrderController(
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            AuthClient authClient) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.authClient = authClient;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<PurchaseOrder>> getAllPurchaseOrders() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = auth.getName();

        logger.info("Solicitud GET /purchase-orders de: {}", currentEmail);

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> {
                    String role = a.getAuthority().toUpperCase();
                    return role.contains("ADMIN") || role.contains("COMPRAS");
                });

        if (isAdmin) {
            return ResponseEntity.ok(purchaseOrderRepository.findAllWithSupplierAndDetailsAndProducts());
        }

        Optional<Long> supplierIdOpt = authClient.getSupplierIdByEmail(currentEmail);

        if (supplierIdOpt.isPresent()) {
            Long sId = supplierIdOpt.get();
            List<PurchaseOrder> orders = purchaseOrderRepository.findBySupplierId(sId);

            List<PurchaseOrder> visibleOrders = orders.stream()
                    .filter(o -> o.getStatus() != OrderStatus.BORRADOR)
                    .toList();

            return ResponseEntity.ok(visibleOrders);
        }

        logger.warn("ALERTA: Usuario {} sin perfil. Retornando vacío.", currentEmail);
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<PurchaseOrder> getPurchaseOrderById(@PathVariable Long id) {
        PurchaseOrder order = purchaseOrderRepository.findByIdWithDetailsAndSupplier(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
        return ResponseEntity.ok(order);
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@Valid @RequestBody PurchaseOrder order) {
        Long supplierId = order.getSupplier().getId();
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proveedor no existe"));

        order.setSupplier(supplier);

        order.setStatus(OrderStatus.BORRADOR);

        if (order.getDetails() != null) {
            order.getDetails().forEach(detail -> detail.setPurchaseOrder(order));
        }

        logger.info("Orden BORRADOR creada ID: {}", order.getId());
        return new ResponseEntity<>(purchaseOrderRepository.save(order), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public ResponseEntity<PurchaseOrder> updatePurchaseOrder(@PathVariable Long id, @Valid @RequestBody PurchaseOrder orderDetails) {
        PurchaseOrder existingOrder = purchaseOrderRepository.findByIdWithDetailsAndSupplier(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));

        if (existingOrder.getStatus() == OrderStatus.CANCELADO || existingOrder.getStatus() == OrderStatus.COMPLETO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede editar una orden finalizada.");
        }

        if (orderDetails.getSupplier() != null && !existingOrder.getSupplier().getId().equals(orderDetails.getSupplier().getId())) {
            Supplier newSupplier = supplierRepository.findById(orderDetails.getSupplier().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nuevo Proveedor no existe"));
            existingOrder.setSupplier(newSupplier);
        }

        existingOrder.setOrderDate(orderDetails.getOrderDate());
        existingOrder.setDeliveryDate(orderDetails.getDeliveryDate());
        existingOrder.setTotalAmount(orderDetails.getTotalAmount());





        existingOrder.getDetails().clear();
        if (orderDetails.getDetails() != null) {
            orderDetails.getDetails().forEach(newDetail -> {
                newDetail.setPurchaseOrder(existingOrder);
                existingOrder.getDetails().add(newDetail);
            });
        }

        return ResponseEntity.ok(purchaseOrderRepository.save(existingOrder));
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<PurchaseOrder> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newStatusStr = payload.get("status");
        if (newStatusStr == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status requerido");

        PurchaseOrder order = purchaseOrderRepository.findByIdWithDetailsAndSupplier(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));

        try {
            OrderStatus newStatusEnum = OrderStatus.valueOf(newStatusStr);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            boolean isSupplier = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().toUpperCase().contains("PROVEEDOR") || a.getAuthority().toUpperCase().contains("SUPPLIER"));

            if (isSupplier) {


                if (order.getStatus() != OrderStatus.PENDIENTE) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo puede responder a órdenes Pendientes.");
                }


                if (newStatusEnum != OrderStatus.CONFIRMADO && newStatusEnum != OrderStatus.RECHAZADO) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acción no permitida: Debe Aceptar (Confirmado) o Rechazar.");
                }
            } else {


                if (order.getStatus() == OrderStatus.BORRADOR && newStatusEnum == OrderStatus.PENDIENTE) {
                    logger.info("Orden {} enviada al proveedor.", id);
                }

                if (newStatusEnum == OrderStatus.CANCELADO) {
                    if (order.getStatus() == OrderStatus.COMPLETO) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede cancelar una orden ya completada.");
                    }
                }
            }

            order.setStatus(newStatusEnum);
            return ResponseEntity.ok(purchaseOrderRepository.save(order));

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido.");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable Long id) {
        if (!purchaseOrderRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada");
        }
        purchaseOrderRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}