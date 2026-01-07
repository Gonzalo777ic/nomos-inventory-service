package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.*;
import com.nomos.inventory.service.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/quotations")
public class QuotationController {

    @Autowired
    private QuotationRepository quotationRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<Quotation> createQuotation(@RequestBody Quotation quotationPayload) {
        Supplier supplier = supplierRepository.findById(quotationPayload.getSupplier().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proveedor no existe"));

        quotationPayload.setSupplier(supplier);
        quotationPayload.setStatus(QuotationStatus.BORRADOR);
        if (quotationPayload.getRequestDate() == null) quotationPayload.setRequestDate(LocalDate.now());

        if (quotationPayload.getDetails() != null) {
            for (QuotationDetail detail : quotationPayload.getDetails()) {
                detail.setQuotation(quotationPayload);

                if (detail.getProduct() != null && detail.getProduct().getId() != null) {
                    Product p = productRepository.findById(detail.getProduct().getId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto ID " + detail.getProduct().getId() + " no existe"));
                    detail.setProduct(p);
                    detail.setProductName(p.getName()); 
                } else {

                    detail.setProduct(null);
                    if (detail.getProductName() == null || detail.getProductName().isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del producto es obligatorio para ítems sugeridos.");
                    }
                }
            }
        }

        return new ResponseEntity<>(quotationRepository.save(quotationPayload), HttpStatus.CREATED);
    }


    @PutMapping("/{id}/details/{detailId}/link-product/{productId}")
    @Transactional
    public ResponseEntity<Quotation> linkProductToDetail(
            @PathVariable Long id,
            @PathVariable Long detailId,
            @PathVariable Long productId) {

        Quotation quotation = quotationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cotización no encontrada"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        QuotationDetail targetDetail = quotation.getDetails().stream()
                .filter(d -> d.getId().equals(detailId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Detalle no encontrado en esta cotización"));

        targetDetail.setProduct(product);
        targetDetail.setProductName(product.getName()); 

        return ResponseEntity.ok(quotationRepository.save(quotation));
    }

    @PostMapping("/{id}/convert-to-order")
    @Transactional
    public ResponseEntity<PurchaseOrder> convertToOrder(@PathVariable Long id) {
        Quotation quotation = quotationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cotización no encontrada"));

        if (quotation.getStatus() == QuotationStatus.CONVERTIDO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta cotización ya fue convertida.");
        }

        boolean hasUnmappedProducts = quotation.getDetails().stream()
                .anyMatch(d -> d.getProduct() == null);

        if (hasUnmappedProducts) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede convertir: Hay productos sugeridos que aún no han sido vinculados al catálogo (Catalogar primero).");
        }

        PurchaseOrder newOrder = new PurchaseOrder();
        newOrder.setSupplier(quotation.getSupplier());
        newOrder.setOrderDate(LocalDate.now());
        newOrder.setDeliveryDate(LocalDate.now().plusDays(7)); 
        newOrder.setStatus(OrderStatus.BORRADOR); 
        Double total = quotation.getTotalEstimated() != null ? quotation.getTotalEstimated() : 0.0;
        newOrder.setTotalAmount(total);
        newOrder.setDetails(new ArrayList<>());

        for (QuotationDetail qDetail : quotation.getDetails()) {
            PurchaseOrderDetail poDetail = new PurchaseOrderDetail();
            poDetail.setPurchaseOrder(newOrder);
            poDetail.setProduct(qDetail.getProduct()); 
            poDetail.setQuantity(qDetail.getQuantity());
            poDetail.setUnitCost(qDetail.getQuotedPrice() != null ? qDetail.getQuotedPrice() : 0.0);

            newOrder.getDetails().add(poDetail);
        }

        quotation.setStatus(QuotationStatus.CONVERTIDO);
        quotationRepository.save(quotation);

        PurchaseOrder savedOrder = purchaseOrderRepository.save(newOrder);
        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping
    public ResponseEntity<List<Quotation>> getAll(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String role) { 

        if (supplierId != null) {
            return ResponseEntity.ok(quotationRepository.findBySupplierId(supplierId));
        }

        return ResponseEntity.ok(quotationRepository.findByStatusNot(QuotationStatus.BORRADOR));
    }

    @PutMapping("/{id}/status")
    @Transactional
    public ResponseEntity<Quotation> updateStatus(@PathVariable Long id, @RequestParam QuotationStatus status) {
        Quotation q = quotationRepository.findById(id).orElseThrow();

        if (q.getStatus() == QuotationStatus.BORRADOR && status == QuotationStatus.ENVIADO) {
            q.setStatus(status);
            q.setRequestDate(LocalDate.now()); 
        } else if (status == QuotationStatus.CANCELADO) {
            q.setStatus(status);
        }
        return ResponseEntity.ok(quotationRepository.save(q));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Quotation> updateQuotation(@PathVariable Long id, @RequestBody Quotation payload) {
        Quotation q = quotationRepository.findById(id).orElseThrow();

        if (q.getStatus() != QuotationStatus.BORRADOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden editar borradores");
        }

        q.setNotes(payload.getNotes());



        q.getDetails().clear();
        if(payload.getDetails() != null) {
            payload.getDetails().forEach(d -> {
                d.setQuotation(q);
                q.getDetails().add(d);
            });
        }

        return ResponseEntity.ok(quotationRepository.save(q));
    }


    @GetMapping("/{id}")
    public ResponseEntity<Quotation> getById(@PathVariable Long id) {
        return ResponseEntity.ok(quotationRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }
}