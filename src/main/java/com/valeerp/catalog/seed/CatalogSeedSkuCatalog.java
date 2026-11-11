package com.valeerp.catalog.seed;

import java.math.BigDecimal;
import java.util.List;

/**
 * Definiciones estáticas del seed de demo (50+ SKUs). Fixtures literales permitidos.
 */
final class CatalogSeedSkuCatalog {

    private CatalogSeedSkuCatalog() {
    }

    record SkuSpec(String sku, String name, String brandCode, String categoryName,
                   String uomAbbr, BigDecimal price, int minStock) {
    }

    static List<SkuSpec> all() {
        return List.of(
                sku("NB-I5-8-256", "Notebook i5 8GB 256SSD", "INT", "Portátiles", "Un", "1899.00", 3),
                sku("NB-I7-16-512", "Notebook i7 16GB 512SSD", "INT", "Portátiles", "Un", "2899.00", 2),
                sku("NB-R5-16-512", "Notebook Ryzen 5 16GB", "AMD", "Portátiles", "Un", "2499.00", 2),
                sku("NB-R7-32-1T", "Notebook Ryzen 7 32GB 1TB", "AMD", "Portátiles", "Un", "3599.00", 1),
                sku("NB-ULTRA-14", "Ultrabook 14\" OLED", "SAM", "Portátiles", "Un", "4299.00", 1),
                sku("MON-24-FHD", "Monitor 24\" FHD IPS", "SAM", "Monitores", "Un", "449.00", 8),
                sku("MON-27-QHD", "Monitor 27\" QHD 144Hz", "SAM", "Monitores", "Un", "899.00", 5),
                sku("MON-32-CURV", "Monitor 32\" Curvo", "SAM", "Monitores", "Un", "1199.00", 4),
                sku("MON-34-UW", "Monitor Ultrawide 34\"", "LG", "Monitores", "Un", "1899.00", 2),
                sku("MON-27-4K", "Monitor 27\" 4K UHD", "LG", "Monitores", "Un", "1599.00", 3),
                sku("KB-MX-KEYS", "Teclado MX Keys", "LOG", "Periféricos", "Un", "399.00", 10),
                sku("MS-MX-MASTER", "Mouse MX Master 3S", "LOG", "Periféricos", "Un", "349.00", 10),
                sku("HS-ZONE-VIB", "Headset Zone Vibe", "LOG", "Periféricos", "Un", "299.00", 8),
                sku("WB-C920", "Webcam C920 HD", "LOG", "Periféricos", "Un", "279.00", 6),
                sku("PAD-DESK-XL", "Pad mouse XL", "GEN", "Periféricos", "Un", "49.90", 20),
                sku("SSD-500-NVME", "SSD NVMe 500GB", "SAM", "Almacenamiento", "Un", "249.00", 15),
                sku("SSD-1T-NVME", "SSD NVMe 1TB", "SAM", "Almacenamiento", "Un", "429.00", 12),
                sku("SSD-2T-NVME", "SSD NVMe 2TB", "SAM", "Almacenamiento", "Un", "799.00", 6),
                sku("HDD-2T-SATA", "HDD 2TB SATA", "WD", "Almacenamiento", "Un", "289.00", 10),
                sku("HDD-4T-SATA", "HDD 4TB SATA", "WD", "Almacenamiento", "Un", "449.00", 8),
                sku("RAM-8-DDR4", "Memoria RAM 8GB DDR4", "KIN", "Componentes", "Un", "129.00", 20),
                sku("RAM-16-DDR4", "Memoria RAM 16GB DDR4", "KIN", "Componentes", "Un", "229.00", 15),
                sku("RAM-32-DDR5", "Memoria RAM 32GB DDR5", "KIN", "Componentes", "Un", "499.00", 8),
                sku("CPU-I5-13400", "Procesador i5-13400", "INT", "Componentes", "Un", "1099.00", 4),
                sku("CPU-R5-7600", "Procesador Ryzen 5 7600", "AMD", "Componentes", "Un", "999.00", 4),
                sku("PSU-650W", "Fuente 650W 80+ Bronze", "COR", "Componentes", "Un", "329.00", 7),
                sku("PSU-850W", "Fuente 850W 80+ Gold", "COR", "Componentes", "Un", "549.00", 5),
                sku("CASE-ATX-M", "Case ATX Mid Tower", "COR", "Componentes", "Un", "279.00", 6),
                sku("MB-B760M", "Placa B760M WiFi", "ASU", "Componentes", "Un", "699.00", 4),
                sku("MB-B650M", "Placa B650M WiFi", "ASU", "Componentes", "Un", "749.00", 4),
                sku("CBL-CAT6-M", "Cable UTP Cat6 (m)", "GEN", "Redes", "M", "1.80", 200),
                sku("CBL-CAT6A-M", "Cable UTP Cat6A (m)", "GEN", "Redes", "M", "2.50", 150),
                sku("CBL-FIBER-M", "Cable fibra OM3 (m)", "GEN", "Redes", "M", "4.90", 80),
                sku("SW-8P-GIGA", "Switch 8 puertos Gigabit", "TP", "Redes", "Un", "119.00", 12),
                sku("SW-24P-GIGA", "Switch 24 puertos Gigabit", "TP", "Redes", "Un", "589.00", 4),
                sku("RT-AX1800", "Router WiFi 6 AX1800", "TP", "Redes", "Un", "249.00", 8),
                sku("AP-AX3000", "Access Point AX3000", "UBQ", "Redes", "Un", "399.00", 5),
                sku("NIC-2.5G", "Tarjeta red 2.5G PCIe", "TP", "Redes", "Un", "89.00", 10),
                sku("PATCH-CAT6-1", "Patch cord Cat6 1m", "GEN", "Redes", "Un", "9.90", 50),
                sku("PATCH-CAT6-3", "Patch cord Cat6 3m", "GEN", "Redes", "Un", "14.90", 40),
                sku("PRT-LASER-A4", "Impresora láser A4", "HP", "Impresión", "Un", "799.00", 3),
                sku("PRT-INK-AIO", "Multifuncional tinta", "HP", "Impresión", "Un", "549.00", 4),
                sku("TONER-BK-STD", "Toner negro estándar", "HP", "Impresión", "Un", "189.00", 15),
                sku("INK-CMYK-SET", "Set tinta CMYK", "HP", "Impresión", "Pqt", "129.00", 12),
                sku("PAPER-A4-500", "Resma papel A4 500h", "GEN", "Impresión", "Pqt", "24.90", 40),
                sku("UPS-1KVA", "UPS 1kVA", "APC", "Energía", "Un", "699.00", 4),
                sku("UPS-1.5KVA", "UPS 1.5kVA", "APC", "Energía", "Un", "999.00", 3),
                sku("PDU-8O", "PDU 8 tomas rack", "APC", "Energía", "Un", "249.00", 5),
                sku("CBL-PWR-C13", "Cable poder C13", "GEN", "Energía", "Un", "19.90", 30),
                sku("SVC-INST-PC", "Servicio instalación PC", "GEN", "Servicios", "Un", "80.00", 0),
                sku("SVC-MNT-1H", "Servicio mantenimiento 1h", "GEN", "Servicios", "Un", "60.00", 0),
                sku("SVC-NET-CFG", "Servicio config red", "GEN", "Servicios", "Un", "120.00", 0),
                sku("CONS-ALCOHOL", "Alcohol isopropílico 1L", "GEN", "Consumibles", "Lt", "35.00", 20),
                sku("CONS-CLEAN-KIT", "Kit limpieza pantallas", "GEN", "Consumibles", "Pqt", "29.90", 25),
                sku("CONS-TAPE-ESD", "Cinta ESD 20m", "GEN", "Consumibles", "Un", "18.50", 30),
                sku("CONS-ZIP-100", "Amarras plásticas x100", "GEN", "Consumibles", "Pqt", "12.90", 40),
                sku("TOOL-CRIMP", "Pinza crimpado RJ45", "GEN", "Herramientas", "Un", "79.00", 6),
                sku("TOOL-TESTER", "Tester cable red", "GEN", "Herramientas", "Un", "59.00", 8),
                sku("TOOL-SCREW-SET", "Set destornilladores", "GEN", "Herramientas", "Pqt", "45.00", 10)
        );
    }

    private static SkuSpec sku(String sku, String name, String brand, String category,
                               String uom, String price, int minStock) {
        return new SkuSpec(sku, name, brand, category, uom, new BigDecimal(price), minStock);
    }
}
