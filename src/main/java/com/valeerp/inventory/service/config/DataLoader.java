package com.valeerp.inventory.service.config;

import com.valeerp.inventory.service.model.Brand;
import com.valeerp.inventory.service.model.Category;
import com.valeerp.inventory.service.model.Product;
import com.valeerp.inventory.service.model.ProductImage;
import com.valeerp.inventory.service.model.ProductSupplier;
import com.valeerp.inventory.service.model.Supplier;
import com.valeerp.inventory.service.model.UnitOfMeasure;
import com.valeerp.inventory.service.model.Warehouse;
import com.valeerp.inventory.service.repository.BrandRepository;
import com.valeerp.inventory.service.repository.CategoryRepository;
import com.valeerp.inventory.service.repository.ProductImageRepository;
import com.valeerp.inventory.service.repository.ProductRepository;
import com.valeerp.inventory.service.repository.ProductSupplierRepository;
import com.valeerp.inventory.service.repository.SupplierRepository;
import com.valeerp.inventory.service.repository.UnitOfMeasureRepository;
import com.valeerp.inventory.service.repository.WarehouseRepository;
import com.valeerp.catalog.CostingMethod;
import com.valeerp.catalog.ProductType;
import com.valeerp.catalog.UomCategory;
import com.valeerp.kernel.Money;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private static final long DEMO_COMPANY_ID = 1L;

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final UnitOfMeasureRepository uomRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSupplierRepository productSupplierRepository;
    private final WarehouseRepository warehouseRepository;

    public DataLoader(BrandRepository brandRepository,
                      CategoryRepository categoryRepository,
                      UnitOfMeasureRepository uomRepository,
                      SupplierRepository supplierRepository,
                      ProductRepository productRepository,
                      ProductImageRepository productImageRepository,
                      ProductSupplierRepository productSupplierRepository,
                      WarehouseRepository warehouseRepository) {
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.uomRepository = uomRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productSupplierRepository = productSupplierRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initializeUoM();
        initializeBrands();
        initializeCategories();
        initializeSuppliers();
        initializeProductsAndRelations();
        initializeWarehouses();
    }

    private void initializeUoM() {
        if (uomRepository.count() > 0) {
            return;
        }
        uomRepository.saveAll(List.of(
                UnitOfMeasure.define("Unidad", "Un", UomCategory.UNIT),
                UnitOfMeasure.define("Kilogramo", "Kg", UomCategory.WEIGHT),
                UnitOfMeasure.define("Litro", "Lt", UomCategory.VOLUME),
                UnitOfMeasure.define("Paquete", "Pqt", UomCategory.UNIT),
                UnitOfMeasure.define("Metro", "M", UomCategory.LENGTH)
        ));
    }

    private void initializeBrands() {
        if (brandRepository.count() > 0) {
            return;
        }
        brandRepository.saveAll(List.of(
                Brand.register("Intel Corporation", "INT", "https://intel.com", "url/logo/intel.png"),
                Brand.register("Samsung Electronics", "SAM", "https://samsung.com", "url/logo/samsung.png"),
                Brand.register("Logitech", "LOG", "https://logitech.com", "url/logo/logitech.png"),
                Brand.register("Generic Network", "GEN", "https://generic.net", "url/logo/generic.png")
        ));
    }

    private void initializeCategories() {
        if (categoryRepository.count() > 0) {
            return;
        }
        Category electronica = categoryRepository.save(
                Category.create("Electrónica", "Dispositivos electrónicos de consumo.", null));
        Category computadoras = categoryRepository.save(
                Category.create("Computadoras", "Equipo de procesamiento y software.", null));
        categoryRepository.save(Category.create("Redes", "Componentes y cableado de red.", electronica.getId()));
        categoryRepository.save(Category.create("Portátiles", "Laptops y ultrabooks.", computadoras.getId()));
        categoryRepository.save(Category.create("Monitores", "Dispositivos de visualización.", electronica.getId()));
        categoryRepository.save(Category.create("Periféricos", "Accesorios de entrada/salida.", computadoras.getId()));
    }

    private void initializeSuppliers() {
        if (supplierRepository.count() > 0) {
            return;
        }
        supplierRepository.saveAll(List.of(
                new Supplier(null, "Tech Global Distributors S.A.", "20512345678",
                        "contacto@techglobal.com", "987654321", "Av. Central 123", "Javier Pérez"),
                new Supplier(null, "Componentes Rápidos SAC", "20498765432",
                        "ventas@rapidos.com", "912345678", "Calle Falsa 45", "Luisa Gómez"),
                new Supplier(null, "Insumos Eléctricos del Norte", "20555555555",
                        "norte@insumos.com", "999888777", "Jr. Trujillo 200", "Carlos Ruiz")
        ));
    }

    private void initializeProductsAndRelations() {
        if (productRepository.count() > 0) {
            return;
        }
        Long brandIdIntel = brandRepository.findByName("Intel Corporation").orElseThrow().getId();
        Long brandIdSamsung = brandRepository.findByName("Samsung Electronics").orElseThrow().getId();
        Long brandIdGeneric = brandRepository.findByName("Generic Network").orElseThrow().getId();
        Long categoryIdPortatiles = categoryRepository.findByName("Portátiles").orElseThrow().getId();
        Long categoryIdMonitores = categoryRepository.findByName("Monitores").orElseThrow().getId();
        Long categoryIdRedes = categoryRepository.findByName("Redes").orElseThrow().getId();
        Long uomIdUnidad = uomRepository.findByAbbreviation("UN").orElseThrow().getId();
        Long uomIdMetro = uomRepository.findByAbbreviation("M").orElseThrow().getId();

        Product p1 = productRepository.save(Product.register(
                "LP12345", "Laptop Core i7 16GB RAM", ProductType.STORABLE,
                brandIdIntel, categoryIdPortatiles, uomIdUnidad,
                "7750123456789", Money.pen(1200.00), 5, CostingMethod.AVERAGE, Money.pen(950.00)));
        Product p2 = productRepository.save(Product.register(
                "MON789", "Monitor LED Curvo 32\"", ProductType.STORABLE,
                brandIdSamsung, categoryIdMonitores, uomIdUnidad,
                "7750987654321", Money.pen(450.00), 10, CostingMethod.STANDARD, Money.pen(380.00)));
        Product p3 = productRepository.save(Product.register(
                "CBLCAT6", "Cable de Red UTP Cat 6 (por metro)", ProductType.CONSUMABLE,
                brandIdGeneric, categoryIdRedes, uomIdMetro,
                "7750000111222", Money.pen(1.50), 50, CostingMethod.AVERAGE, Money.pen(0.85)));

        productImageRepository.saveAll(List.of(
                ProductImage.attach(p1.getId(), null,
                        "https://media.falabella.com/falabellaPE/20727034_001/w=800,h=800,fit=pad",
                        "catalog/demo/lp12345.webp", "image/webp", 120_000L, "lp12345.webp", true, 0),
                ProductImage.attach(p2.getId(), null,
                        "https://http2.mlstatic.com/D_NQ_NP_2X_790766-MLA96253572256_102025-F.webp",
                        "catalog/demo/mon789.webp", "image/webp", 98_000L, "mon789.webp", true, 0),
                ProductImage.attach(p3.getId(), null,
                        "https://media.falabella.com/falabellaPE/117697871_01/w=1500,h=1500,fit=pad",
                        "catalog/demo/cblcat6.webp", "image/webp", 80_000L, "cblcat6.webp", true, 0)
        ));

        Supplier supplier1 = supplierRepository.findByTaxId("20512345678").orElseThrow();
        Supplier supplier2 = supplierRepository.findByTaxId("20498765432").orElseThrow();
        Supplier supplier3 = supplierRepository.findByTaxId("20555555555").orElseThrow();

        productSupplierRepository.save(new ProductSupplier(
                p1.getId(), supplier1.getId(), "ITL-LP-22", Money.pen(950.00), 7, true, true));
        productSupplierRepository.save(new ProductSupplier(
                p1.getId(), supplier2.getId(), "RAP-LAP-i7", Money.pen(980.00), 5, false, true));
        productSupplierRepository.save(new ProductSupplier(
                p2.getId(), supplier1.getId(), "SAM-CURV-32", Money.pen(380.00), 10, true, true));
        productSupplierRepository.save(new ProductSupplier(
                p3.getId(), supplier2.getId(), "C-RED-RAP", Money.pen(0.85), 4, true, true));
        productSupplierRepository.save(new ProductSupplier(
                p3.getId(), supplier1.getId(), "TG-C6-EST", Money.pen(1.10), 8, false, true));
        productSupplierRepository.save(new ProductSupplier(
                p3.getId(), supplier3.getId(), "NOR-CAT6", Money.pen(0.90), 15, false, true));
    }

    private void initializeWarehouses() {
        if (warehouseRepository.count() > 0) {
            return;
        }
        warehouseRepository.save(Warehouse.open(
                DEMO_COMPANY_ID, null, "MAIN", "Almacén Principal", "Av. Los Tulipanes 100", true));
        warehouseRepository.save(Warehouse.open(
                DEMO_COMPANY_ID, null, "STORE", "Tienda Central", "Jr. Unión 500", false));
    }
}
