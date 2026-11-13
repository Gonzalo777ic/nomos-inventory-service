package com.valeerp.procurement;

import com.valeerp.inventory.service.model.OrderStatus;
import com.valeerp.inventory.service.model.Product;
import com.valeerp.inventory.service.model.PurchaseOrder;
import com.valeerp.inventory.service.model.Quotation;
import com.valeerp.inventory.service.model.Supplier;
import com.valeerp.inventory.service.model.Warehouse;
import com.valeerp.inventory.service.repository.ProductRepository;
import com.valeerp.inventory.service.repository.PurchaseOrderRepository;
import com.valeerp.inventory.service.repository.QuotationRepository;
import com.valeerp.inventory.service.repository.SupplierRepository;
import com.valeerp.inventory.service.repository.WarehouseRepository;
import com.valeerp.kernel.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Fixture end-to-end del flujo de compra dentro de este servicio: RFQ → OC → recepción.
 * No llama a otros microservicios. Activar con {@code valeerp.procurement.seed.enabled=true}.
 */
@Component
@Order(200)
@ConditionalOnProperty(prefix = "valeerp.procurement.seed", name = "enabled", havingValue = "true")
public class PurchaseFlowFixtureSeeder implements ApplicationRunner {

    public static final String FIXTURE_MARKER = "FIXTURE:RFQ-PO-RECEIPT";

    private static final Logger log = LoggerFactory.getLogger(PurchaseFlowFixtureSeeder.class);

    private static final Long DEMO_COMPANY_ID = 1L;

    private final QuotationRepository quotationRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final PurchaseReceiptService purchaseReceiptService;
    private final ProcurementProperties procurementProperties;
    private final FrameworkAgreementService frameworkAgreementService;

    public PurchaseFlowFixtureSeeder(QuotationRepository quotationRepository,
                                     PurchaseOrderRepository purchaseOrderRepository,
                                     SupplierRepository supplierRepository,
                                     ProductRepository productRepository,
                                     WarehouseRepository warehouseRepository,
                                     PurchaseReceiptService purchaseReceiptService,
                                     ProcurementProperties procurementProperties,
                                     FrameworkAgreementService frameworkAgreementService) {
        this.quotationRepository = quotationRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.purchaseReceiptService = purchaseReceiptService;
        this.procurementProperties = procurementProperties;
        this.frameworkAgreementService = frameworkAgreementService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (quotationRepository.findAll().stream().anyMatch(q -> FIXTURE_MARKER.equals(q.getNotes()))) {
            log.info("Purchase flow fixture already present ({}); skipping", FIXTURE_MARKER);
            return;
        }
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            log.warn("Purchase flow fixture skipped: need products (enable valeerp.catalog.seed.enabled)");
            return;
        }
        ensureMainWarehouse();
        Supplier supplier = ensureDemoSupplier();
        Product product = products.get(0);
        LocalDate today = LocalDate.now();

        seedFrameworkAgreement(supplier, product, today);

        Quotation rfq = Quotation.draft(supplier, today, today.plusDays(30), FIXTURE_MARKER);
        rfq.addCatalogLine(product, 5, Money.pen(100.00), 10, today.plusDays(10));
        rfq.send(today);
        rfq.markResponded();
        rfq.accept();
        rfq = quotationRepository.save(rfq);

        PurchaseOrder order = rfq.convertToPurchaseOrder(procurementProperties.getDefaultLeadDays(), today);
        quotationRepository.save(rfq);
        order = purchaseOrderRepository.save(order);

        order.submit(procurementProperties.approvalThreshold());
        if (order.getStatus() == OrderStatus.PENDIENTE_APROBACION) {
            order.approve();
        }
        order.requestTransition(OrderStatus.CONFIRMADO, true);
        order = purchaseOrderRepository.save(order);
        order = purchaseReceiptService.receive(order.getId());

        log.info("Purchase flow fixture ready: rfqId={} poId={} status={} marker={}",
                rfq.getId(), order.getId(), order.getStatus(), FIXTURE_MARKER);
    }

    private Supplier ensureDemoSupplier() {
        return supplierRepository.findByTaxId("20999999001").orElseGet(() -> {
            Supplier supplier = new Supplier(
                    null,
                    "Proveedor Fixture Compras SAC",
                    "20999999001",
                    "compras-fixture@example.com",
                    "999000111",
                    "Av. Demo 100",
                    "Contacto Fixture"
            );
            return supplierRepository.save(supplier);
        });
    }

    private void ensureMainWarehouse() {
        if (warehouseRepository.findByIsMainTrue().isPresent()) {
            return;
        }
        warehouseRepository.save(Warehouse.open(
                DEMO_COMPANY_ID,
                null,
                "WH-MAIN",
                "Almacén principal fixture",
                "Lima",
                true
        ));
    }

    private void seedFrameworkAgreement(Supplier supplier, Product product, LocalDate today) {
        try {
            var created = frameworkAgreementService.create(new FrameworkAgreementDtos.CreateRequest(
                    "FA-FIXTURE-001",
                    supplier.getId(),
                    today.minusDays(1),
                    today.plusYears(1),
                    "Acuerdo marco demo del flujo de compra",
                    List.of(new FrameworkAgreementDtos.LineRequest(
                            product.getId(),
                            Money.pen(100.00),
                            today.minusDays(1),
                            today.plusMonths(6)
                    ))
            ));
            frameworkAgreementService.activate(created.id());
        } catch (RuntimeException ex) {
            log.debug("Framework agreement fixture skipped: {}", ex.getMessage());
        }
    }
}
