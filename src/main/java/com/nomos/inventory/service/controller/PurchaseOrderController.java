package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.OrderStatus;
import com.nomos.inventory.service.model.PurchaseOrder;
import com.nomos.inventory.service.model.Supplier;
import com.nomos.inventory.service.repository.PurchaseOrderRepository;
import com.nomos.inventory.service.repository.SupplierRepository;
import com.valeerp.kernel.DomainException;
import com.valeerp.procurement.PurchaseReceiptService;
import com.valeerp.procurement.SupplierAccountService;
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
    private final SupplierAccountService supplierAccountService;
    private final PurchaseReceiptService purchaseReceiptService;

    @Autowired
    public PurchaseOrderController(
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierRepository supplierRepository,
            SupplierAccountService supplierAccountService,
            PurchaseReceiptService purchaseReceiptService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.supplierAccountService = supplierAccountService;
        this.purchaseReceiptService = purchaseReceiptService;
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

        Optional<Long> supplierIdOpt = supplierAccountService.supplierIdOf(currentEmail);
        if (supplierIdOpt.isEmpty()) {
            throw new DomainException("El usuario proveedor no tiene una cuenta de abastecimiento vinculada");
        }
        Long supplierId = supplierIdOpt.get();
        List<PurchaseOrder> visibleOrders = purchaseOrderRepository.findBySupplierId(supplierId).stream()
                .filter(order -> order.getStatus() != OrderStatus.BORRADOR)
                .toList();
        return ResponseEntity.ok(visibleOrders);
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
        order.recalculateTotal();

        logger.info("Orden BORRADOR creada ID: {}", order.getId());
        return new ResponseEntity<>(purchaseOrderRepository.save(order), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_COMPRAS')")
    public ResponseEntity<PurchaseOrder> updatePurchaseOrder(@PathVariable Long id, @Valid @RequestBody PurchaseOrder orderDetails) {
        PurchaseOrder existingOrder = purchaseOrderRepository.findByIdWithDetailsAndSupplier(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));

        existingOrder.assertEditable();

        if (orderDetails.getSupplier() != null && !existingOrder.getSupplier().getId().equals(orderDetails.getSupplier().getId())) {
            Supplier newSupplier = supplierRepository.findById(orderDetails.getSupplier().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nuevo Proveedor no existe"));
            existingOrder.setSupplier(newSupplier);
        }

        existingOrder.setOrderDate(orderDetails.getOrderDate());
        existingOrder.setDeliveryDate(orderDetails.getDeliveryDate());
        existingOrder.getDetails().clear();
        if (orderDetails.getDetails() != null) {
            orderDetails.getDetails().forEach(newDetail -> {
                newDetail.setPurchaseOrder(existingOrder);
                existingOrder.getDetails().add(newDetail);
            });
        }
        existingOrder.recalculateTotal();

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
            OrderStatus newStatus = OrderStatus.valueOf(newStatusStr);
            if (newStatus == OrderStatus.COMPLETO) {
                return ResponseEntity.ok(purchaseReceiptService.receive(id));
            }
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isSupplier = auth.getAuthorities().stream()
                    .anyMatch(authority -> {
                        String role = authority.getAuthority().toUpperCase();
                        return role.contains("PROVEEDOR") || role.contains("SUPPLIER");
                    });
            order.requestTransition(newStatus, isSupplier);
            return ResponseEntity.ok(purchaseOrderRepository.save(order));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido.");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
        order.cancelOpen();
        purchaseOrderRepository.save(order);
        return ResponseEntity.noContent().build();
    }
}