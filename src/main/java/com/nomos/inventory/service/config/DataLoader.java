package com.nomos.inventory.service.config;

import com.nomos.inventory.service.model.*;
import com.nomos.inventory.service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final UnitOfMeasureRepository uomRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSupplierRepository productSupplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final StoreScheduleRepository storeScheduleRepository;
    private final AnnouncementRepository announcementRepository;
    private final ClosureDateRepository closureDateRepository;


    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("--- Ejecutando inicialización de datos de la base de datos (DataLoader) ---");

        initializeUoM();
        initializeBrands();
        initializeCategories();
        initializeSuppliers();

        initializeProductsAndRelations();

        initializeWarehouses();
        initializeInventory();

        initializeStoreSchedule();
        initializeAnnouncementsAndClosures();

        System.out.println("--- Inicialización completada. ---");
    }

    /*
     * MÉTODOS DE INICIALIZACIÓN
     */

    private void initializeUoM() {
        if (uomRepository.count() == 0) {
            uomRepository.saveAll(Arrays.asList(
                    new UnitOfMeasure(null, "Unidad", "Un"),
                    new UnitOfMeasure(null, "Kilogramo", "Kg"),
                    new UnitOfMeasure(null, "Litro", "Lt"),
                    new UnitOfMeasure(null, "Paquete", "Pqt"),
                    new UnitOfMeasure(null, "Metro", "M") // Añadida para el cable
            ));
        }
    }

    private void initializeBrands() {
        if (brandRepository.count() == 0) {
            brandRepository.saveAll(Arrays.asList(
                    new Brand(null, "Intel Corporation", "INT", "https://intel.com", "url/logo/intel.png"),
                    new Brand(null, "Samsung Electronics", "SAM", "https://samsung.com", "url/logo/samsung.png"),
                    new Brand(null, "Logitech", "LOG", "https://logitech.com", "url/logo/logitech.png"),
                    new Brand(null, "Generic Network", "GEN", "https://generic.net", "url/logo/generic.png") // Nueva marca
            ));
        }
    }

    private void initializeCategories() {
        if (categoryRepository.count() == 0) {

            Category electronica = categoryRepository.save(new Category(null, "Electrónica", "Dispositivos electrónicos de consumo.", null));
            Category computadoras = categoryRepository.save(new Category(null, "Computadoras", "Equipo de procesamiento y software.", null));
            Category red = categoryRepository.save(new Category(null, "Redes", "Componentes y cableado de red.", electronica)); // Nueva Categoría

            categoryRepository.save(new Category(null, "Portátiles", "Laptops y ultrabooks.", computadoras));
            categoryRepository.save(new Category(null, "Monitores", "Dispositivos de visualización.", electronica));
            categoryRepository.save(new Category(null, "Periféricos", "Accesorios de entrada/salida.", computadoras));
        }
    }

    private void initializeSuppliers() {
        if (supplierRepository.count() == 0) {
            supplierRepository.saveAll(Arrays.asList(
                    new Supplier(null, "Tech Global Distributors S.A.", "20512345678", "contacto@techglobal.com", "987654321", "Av. Central 123", "Javier Pérez"),
                    new Supplier(null, "Componentes Rápidos SAC", "20498765432", "ventas@rapidos.com", "912345678", "Calle Falsa 45", "Luisa Gómez"),
                    new Supplier(null, "Insumos Eléctricos del Norte", "20555555555", "norte@insumos.com", "999888777", "Jr. Trujillo 200", "Carlos Ruiz") // Tercer proveedor
            ));
        }
    }

    private void initializeProductsAndRelations() {
        if (productRepository.count() == 0) {

            Long brandIdIntel = brandRepository.findByName("Intel Corporation").orElseThrow().getId();
            Long brandIdSamsung = brandRepository.findByName("Samsung Electronics").orElseThrow().getId();
            Long brandIdGeneric = brandRepository.findByName("Generic Network").orElseThrow().getId();

            Long categoryIdPortatiles = categoryRepository.findByName("Portátiles").orElseThrow().getId();
            Long categoryIdMonitores = categoryRepository.findByName("Monitores").orElseThrow().getId();
            Long categoryIdRedes = categoryRepository.findByName("Redes").orElseThrow().getId();

            Long uomIdUnidad = uomRepository.findByAbbreviation("Un").orElseThrow().getId();
            Long uomIdMetro = uomRepository.findByAbbreviation("M").orElseThrow().getId();

            Product p1 = new Product(null, "LP12345", "Laptop Core i7 16GB RAM", brandIdIntel, 1200.00, 5, categoryIdPortatiles, uomIdUnidad, null, null, null, null, null);
            productRepository.save(p1);

            Product p2 = new Product(null, "MON789", "Monitor LED Curvo 32\"", brandIdSamsung, 450.00, 10, categoryIdMonitores, uomIdUnidad, null, null, null, null, null);
            productRepository.save(p2);

            Product p3 = new Product(null, "CBLCAT6", "Cable de Red UTP Cat 6 (por metro)", brandIdGeneric, 1.50, 50, categoryIdRedes, uomIdMetro, null, null, null, null, null);
            productRepository.save(p3);

            productImageRepository.saveAll(Arrays.asList(
                    new ProductImage(null, p1.getId(), "https://media.falabella.com/falabellaPE/20727034_001/w=800,h=800,fit=pad", true, 0, null),
                    new ProductImage(null, p2.getId(), "https://http2.mlstatic.com/D_NQ_NP_2X_790766-MLA96253572256_102025-F.webp", true, 0, null),
                    new ProductImage(null, p3.getId(), "https://media.falabella.com/falabellaPE/117697871_01/w=1500,h=1500,fit=pad", true, 0, null)
            ));

            Supplier supplier1 = supplierRepository.findByTaxId("20512345678").orElseThrow(); // Tech Global
            Supplier supplier2 = supplierRepository.findByTaxId("20498765432").orElseThrow(); // Componentes Rápidos
            Supplier supplier3 = supplierRepository.findByTaxId("20555555555").orElseThrow(); // Insumos Norte


            productSupplierRepository.save(new ProductSupplier(p1.getId(), supplier1.getId(), "ITL-LP-22", 950.00, 7, true, true));

            productSupplierRepository.save(new ProductSupplier(p1.getId(), supplier2.getId(), "RAP-LAP-i7", 980.00, 5, false, true));


            productSupplierRepository.save(new ProductSupplier(p2.getId(), supplier1.getId(), "SAM-CURV-32", 380.00, 10, true, true));


            productSupplierRepository.save(new ProductSupplier(p3.getId(), supplier2.getId(), "C-RED-RAP", 0.85, 4, true, true));

            productSupplierRepository.save(new ProductSupplier(p3.getId(), supplier1.getId(), "TG-C6-EST", 1.10, 8, false, true));

            productSupplierRepository.save(new ProductSupplier(p3.getId(), supplier3.getId(), "NOR-CAT6", 0.90, 15, false, true));
        }
    }

    private void initializeWarehouses() {
        if (warehouseRepository.count() == 0) {
            warehouseRepository.saveAll(Arrays.asList(
                    new Warehouse(null, "Almacén Principal", "Av. Los Tulipanes 100", true),
                    new Warehouse(null, "Tienda Central", "Jr. Unión 500", false)
            ));
        }
    }

    private void initializeInventory() {
        if (inventoryItemRepository.count() == 0) {
            Product p1 = productRepository.findBySku("LP12345").orElseThrow();
            Product p2 = productRepository.findBySku("MON789").orElseThrow();
            Product p3 = productRepository.findBySku("CBLCAT6").orElseThrow();
            Warehouse w1 = warehouseRepository.findByName("Almacén Principal").orElseThrow();
            Warehouse w2 = warehouseRepository.findByName("Tienda Central").orElseThrow();

            inventoryItemRepository.save(new InventoryItem(null, p1, w1, 25, 950.00, "LOTE-A2025", null, "A3-E2", LocalDateTime.now().minusDays(30)));

            inventoryItemRepository.save(new InventoryItem(null, p2, w1, 40, 380.00, "LOTE-M2025", LocalDate.now().plusYears(2), "A1-E5", LocalDateTime.now().minusDays(45)));

            inventoryItemRepository.save(new InventoryItem(null, p3, w1, 5000, 0.85, "LOTE-CAT6-1", null, "B1-C4", LocalDateTime.now().minusDays(10)));
        }
    }

    private void initializeStoreSchedule() {
        if (storeScheduleRepository.count() == 0) {
            storeScheduleRepository.saveAll(Arrays.asList(
                    new StoreSchedule(null, DayOfWeek.LUNES, LocalTime.of(9, 0), LocalTime.of(18, 0), true),
                    new StoreSchedule(null, DayOfWeek.MARTES, LocalTime.of(9, 0), LocalTime.of(18, 0), true),
                    new StoreSchedule(null, DayOfWeek.MIERCOLES, LocalTime.of(9, 0), LocalTime.of(18, 0), true),
                    new StoreSchedule(null, DayOfWeek.JUEVES, LocalTime.of(9, 0), LocalTime.of(18, 0), true),
                    new StoreSchedule(null, DayOfWeek.VIERNES, LocalTime.of(9, 0), LocalTime.of(18, 0), true),
                    new StoreSchedule(null, DayOfWeek.SABADO, LocalTime.of(9, 0), LocalTime.of(13, 0), true),
                    new StoreSchedule(null, DayOfWeek.DOMINGO, LocalTime.of(0, 0), LocalTime.of(0, 0), false) // Cerrado
            ));
        }
    }

    private void initializeAnnouncementsAndClosures() {
        if (announcementRepository.count() == 0) {
            announcementRepository.saveAll(Arrays.asList(
                    new Announcement(null, "¡ENVÍO GRATIS!", "Envío gratuito en todos los pedidos superiores a $500 hasta fin de mes.", LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(25), AnnouncementType.BANNER, true),
                    new Announcement(null, "Aviso de Mantenimiento", "Nuestro sitio web estará en mantenimiento el sábado de 00:00 a 06:00.", LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), AnnouncementType.MODAL, true)
            ));
        }

        if (closureDateRepository.count() == 0) {
            closureDateRepository.saveAll(Arrays.asList(
                    new ClosureDate(null, LocalDate.now().plusDays(15), "Feriado Nacional", true, null),
                    new ClosureDate(null, LocalDate.now().plusDays(30), "Inventario Parcial", false, LocalTime.of(14, 0)) // Cierra a las 2 PM
            ));
        }
    }
}