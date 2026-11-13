package com.valeerp.procurement;

import com.valeerp.inventory.service.model.Product;
import com.valeerp.inventory.service.model.PurchaseOrder;
import com.valeerp.inventory.service.model.PurchaseOrderDetail;
import com.valeerp.inventory.service.model.Supplier;
import com.valeerp.inventory.service.repository.ProductRepository;
import com.valeerp.inventory.service.repository.PurchaseOrderRepository;
import com.valeerp.inventory.service.repository.SupplierRepository;
import com.valeerp.kernel.DomainException;
import com.valeerp.procurement.PurchaseOrderDtos.CreatePurchaseOrderRequest;
import com.valeerp.procurement.PurchaseOrderDtos.LineRequest;
import com.valeerp.procurement.PurchaseOrderDtos.PurchaseOrderLineResponse;
import com.valeerp.procurement.PurchaseOrderDtos.PurchaseOrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ProcurementProperties procurementProperties;
    private final PurchaseReceiptService purchaseReceiptService;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                SupplierRepository supplierRepository,
                                ProductRepository productRepository,
                                ProcurementProperties procurementProperties,
                                PurchaseReceiptService purchaseReceiptService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.procurementProperties = procurementProperties;
        this.purchaseReceiptService = purchaseReceiptService;
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse get(Long id) {
        return toResponse(requireOrder(id));
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> list() {
        return purchaseOrderRepository.findAllWithSupplierAndDetailsAndProducts().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PurchaseOrderResponse create(CreatePurchaseOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new DomainException("Proveedor no existe"));
        PurchaseOrder order = PurchaseOrder.draft(supplier, request.orderDate(), request.deliveryDate());
        for (LineRequest line : request.lines()) {
            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new DomainException("Producto no existe: " + line.productId()));
            PurchaseOrderDetail detail = new PurchaseOrderDetail();
            detail.setPurchaseOrder(order);
            detail.setProduct(product);
            detail.setQuantity(line.quantity());
            detail.setReceivedQuantity(0);
            detail.setUnitCost(line.unitCost());
            detail.setLeadTimeDays(line.leadTimeDays());
            detail.setPromisedDeliveryDate(line.promisedDeliveryDate() != null
                    ? line.promisedDeliveryDate()
                    : (line.leadTimeDays() == null
                    ? null
                    : request.orderDate().plusDays(line.leadTimeDays())));
            order.getDetails().add(detail);
        }
        order.recalculateTotal();
        return toResponse(purchaseOrderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse submit(Long id) {
        PurchaseOrder order = requireOrder(id);
        order.submit(procurementProperties.approvalThreshold());
        return toResponse(purchaseOrderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse approve(Long id) {
        PurchaseOrder order = requireOrder(id);
        order.approve();
        return toResponse(purchaseOrderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse rejectApproval(Long id) {
        PurchaseOrder order = requireOrder(id);
        order.rejectApproval();
        return toResponse(purchaseOrderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse cancel(Long id) {
        PurchaseOrder order = requireOrder(id);
        order.cancelOpen();
        return toResponse(purchaseOrderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse receive(Long id, PurchaseOrderDtos.ReceivePurchaseOrderRequest request) {
        return toResponse(purchaseReceiptService.receive(id, request));
    }

    private PurchaseOrder requireOrder(Long id) {
        return purchaseOrderRepository.findByIdWithDetailsAndSupplier(id)
                .orElseThrow(() -> new DomainException("Orden de compra no encontrada"));
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder order) {
        List<PurchaseOrderLineResponse> lines = new ArrayList<>();
        if (order.getDetails() != null) {
            for (PurchaseOrderDetail detail : order.getDetails()) {
                lines.add(new PurchaseOrderLineResponse(
                        detail.getId(),
                        detail.getProduct() == null ? null : detail.getProduct().getId(),
                        detail.getProduct() == null ? null : detail.getProduct().getName(),
                        detail.getQuantity(),
                        detail.getReceivedQuantity(),
                        detail.residualQuantity(),
                        detail.getUnitCost(),
                        detail.getLeadTimeDays(),
                        detail.getPromisedDeliveryDate()));
            }
        }
        return new PurchaseOrderResponse(
                order.getId(),
                order.getSupplier() == null ? null : order.getSupplier().getId(),
                order.getSupplier() == null ? null : order.getSupplier().getName(),
                order.getOrderDate(),
                order.getDeliveryDate(),
                order.getTotalAmount(),
                order.getStatus() == null ? null : order.getStatus().name(),
                order.totalResidualQuantity(),
                lines);
    }
}
