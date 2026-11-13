package com.valeerp.catalog.seed;

import com.valeerp.inventory.service.model.Brand;
import com.valeerp.inventory.service.model.Category;
import com.valeerp.inventory.service.model.Product;
import com.valeerp.inventory.service.model.UnitOfMeasure;
import com.valeerp.inventory.service.repository.BrandRepository;
import com.valeerp.inventory.service.repository.CategoryRepository;
import com.valeerp.inventory.service.repository.ProductRepository;
import com.valeerp.inventory.service.repository.UnitOfMeasureRepository;
import com.valeerp.catalog.CostingMethod;
import com.valeerp.catalog.UomCategory;
import com.valeerp.catalog.seed.CatalogSeedSkuCatalog.SkuSpec;
import com.valeerp.kernel.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seed local de catálogo (50+ SKUs). Solo corre si {@code valeerp.catalog.seed.enabled=true}.
 * No borra datos: completa maestros faltantes e inserta SKUs ausentes vía {@link Product#register}.
 */
@Component
@Order(100)
@ConditionalOnProperty(prefix = "valeerp.catalog.seed", name = "enabled", havingValue = "true")
public class CatalogDemoSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogDemoSeeder.class);

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final UnitOfMeasureRepository uomRepository;
    private final ProductRepository productRepository;

    public CatalogDemoSeeder(BrandRepository brandRepository,
                             CategoryRepository categoryRepository,
                             UnitOfMeasureRepository uomRepository,
                             ProductRepository productRepository) {
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.uomRepository = uomRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("CatalogDemoSeeder: iniciando seed local de catálogo");
        Map<String, Long> brands = ensureBrands();
        Map<String, Long> categories = ensureCategories();
        Map<String, Long> uoms = ensureUoms();
        int created = 0;
        for (SkuSpec spec : CatalogSeedSkuCatalog.all()) {
            String sku = spec.sku().trim().toUpperCase();
            if (productRepository.existsBySku(sku)) {
                continue;
            }
            Long brandId = brands.get(spec.brandCode().toUpperCase());
            Long categoryId = categories.get(spec.categoryName());
            Long uomId = uoms.get(spec.uomAbbr().trim().toUpperCase());
            if (brandId == null || categoryId == null || uomId == null) {
                throw new IllegalStateException("Falta maestro para SKU " + sku
                        + " brand=" + brandId + " cat=" + categoryId + " uom=" + uomId);
            }
            Money listPrice = Money.pen(spec.price());
            Product product = Product.register(
                    sku,
                    spec.name(),
                    brandId,
                    categoryId,
                    uomId,
                    null,
                    listPrice,
                    spec.minStock(),
                    CostingMethod.STANDARD,
                    Money.zero(listPrice.currency())
            );
            productRepository.save(product);
            created++;
        }
        log.info("CatalogDemoSeeder: SKUs nuevos={}, total productos={}", created, productRepository.count());
    }

    private Map<String, Long> ensureBrands() {
        List<String[]> defs = List.of(
                new String[]{"Intel Corporation", "INT"},
                new String[]{"AMD", "AMD"},
                new String[]{"Samsung Electronics", "SAM"},
                new String[]{"LG Electronics", "LG"},
                new String[]{"Logitech", "LOG"},
                new String[]{"Generic Network", "GEN"},
                new String[]{"Western Digital", "WD"},
                new String[]{"Kingston", "KIN"},
                new String[]{"Corsair", "COR"},
                new String[]{"ASUS", "ASU"},
                new String[]{"TP-Link", "TP"},
                new String[]{"Ubiquiti", "UBQ"},
                new String[]{"HP Inc.", "HP"},
                new String[]{"APC by Schneider", "APC"}
        );
        Map<String, Long> byCode = new HashMap<>();
        for (String[] def : defs) {
            Brand saved = brandRepository.findByName(def[0])
                    .orElseGet(() -> brandRepository.save(Brand.register(def[0], def[1], null, null)));
            byCode.put(saved.getCode(), saved.getId());
        }
        return byCode;
    }

    private Map<String, Long> ensureCategories() {
        Category electronica = categoryNamed("Electrónica", "Dispositivos electrónicos de consumo.", null);
        Category computadoras = categoryNamed("Computadoras", "Equipo de procesamiento.", null);
        Map<String, Long> byName = new HashMap<>();
        byName.put(electronica.getName(), electronica.getId());
        byName.put(computadoras.getName(), computadoras.getId());
        for (Category child : List.of(
                categoryNamed("Portátiles", "Laptops y ultrabooks.", electronica.getId()),
                categoryNamed("Monitores", "Pantallas.", electronica.getId()),
                categoryNamed("Periféricos", "Entrada/salida.", computadoras.getId()),
                categoryNamed("Almacenamiento", "SSD/HDD.", electronica.getId()),
                categoryNamed("Componentes", "CPU/RAM/placas.", computadoras.getId()),
                categoryNamed("Redes", "Cableado y equipos de red.", electronica.getId()),
                categoryNamed("Impresión", "Impresoras e insumos.", electronica.getId()),
                categoryNamed("Energía", "UPS y poder.", electronica.getId()),
                categoryNamed("Servicios", "Servicios técnicos.", null),
                categoryNamed("Consumibles", "Insumos de taller.", null),
                categoryNamed("Herramientas", "Herramientas de campo.", null)
        )) {
            byName.put(child.getName(), child.getId());
        }
        return byName;
    }

    private Map<String, Long> ensureUoms() {
        Map<String, Long> byAbbr = new HashMap<>();
        record UomDef(String name, String abbr, UomCategory category) {
        }
        for (UomDef def : List.of(
                new UomDef("Unidad", "Un", UomCategory.UNIT),
                new UomDef("Caja", "CJA", UomCategory.UNIT),
                new UomDef("Kilogramo", "Kg", UomCategory.WEIGHT),
                new UomDef("Litro", "Lt", UomCategory.VOLUME),
                new UomDef("Paquete", "Pqt", UomCategory.UNIT),
                new UomDef("Metro", "M", UomCategory.LENGTH)
        )) {
            UnitOfMeasure saved = uomRepository.findByAbbreviation(def.abbr().trim().toUpperCase())
                    .or(() -> uomRepository.findByAbbreviation(def.abbr()))
                    .orElseGet(() -> uomRepository.save(UnitOfMeasure.define(def.name(), def.abbr(), def.category())));
            byAbbr.put(saved.getAbbreviation(), saved.getId());
        }
        return byAbbr;
    }

    private Category categoryNamed(String name, String description, Long parentId) {
        return categoryRepository.findByName(name).orElseGet(() ->
                categoryRepository.save(Category.create(name, description, parentId)));
    }
}
