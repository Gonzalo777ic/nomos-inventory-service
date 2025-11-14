package com.nomos.inventory.service.controller;

import com.nomos.inventory.service.model.ProductImage;
import com.nomos.inventory.service.repository.ProductImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;


@RestController
@RequestMapping("/api/inventory/product-images")
// NOTE: Se ha eliminado @RequiredArgsConstructor para usar un constructor manual.
public class ProductImageController {

    private final ProductImageRepository imageRepository;

    // 💡 LÓGICA DE ALMACENAMIENTO DE ARCHIVOS INTEGRADA EN EL CONTROLADOR:

    // 1. Configuración de Rutas y URL Base (Se inyectan desde application.properties)
    private final Path fileStorageLocation = Paths.get("uploads/images/").toAbsolutePath().normalize();

    // URL Base para que la imagen sea accesible públicamente.
    // Usaremos http://localhost:8080/images/ como prefijo (ver configuración estática de Spring)
    private final String imagePublicPath = "/images/";

    // Se inyecta la URL base del servidor (ej: http://localhost:8080)
    private final String baseUrl;

    // Constructor manual para inyectar dependencias, valores y configurar el directorio
    public ProductImageController(
            ProductImageRepository imageRepository,
            @Value("${server.base-url:http://localhost:8080}") String baseUrl) {

        this.imageRepository = imageRepository;
        this.baseUrl = baseUrl;

        // Inicialización: Crea el directorio de almacenamiento si no existe
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            // Es importante manejar esta excepción ya que es crítica para el funcionamiento.
            throw new RuntimeException("Error al inicializar el directorio de almacenamiento de imágenes.", ex);
        }
    }

    // 2. Método privado para la lógica de almacenamiento de archivos
    /**
     * Guarda el archivo físicamente en 'uploads/images/' y devuelve la URL pública completa.
     * @param file El archivo MultipartFile.
     * @return La URL pública completa (ej: http://localhost:8080/images/unique-id.png).
     */
    private String storeFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Genera un nombre único para evitar colisiones
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Comprueba si el nombre del archivo contiene caracteres inválidos
        if (uniqueFilename.contains("..")) {
            throw new IOException("El nombre del archivo contiene una secuencia de ruta inválida.");
        }

        // Copia el archivo al destino
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Devuelve la URL pública y accesible para el frontend
        return this.baseUrl + this.imagePublicPath + uniqueFilename;
    }


    // ----------------------------------------------------------------------------------
    // ENDPOINTS
    // ----------------------------------------------------------------------------------


    // 1. GET ALL by Product
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<List<ProductImage>> getImagesByProduct(@PathVariable Long productId) {
        List<ProductImage> images = imageRepository.findByProductIdOrderBySortOrderAsc(productId);
        return ResponseEntity.ok(images);
    }

    // 2. POST (CREATE) - Añadir una nueva imagen (Usado para URL externas que ya son públicas)
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ProductImage> addProductImage(@Valid @RequestBody ProductImage image) {
        // Lógica de negocio para asegurar solo UNA imagen es 'isMain=true' por producto
        if (image.getIsMain() != null && image.getIsMain()) {
            // Desmarcar cualquier otra imagen principal para este producto
            imageRepository.findByProductIdAndIsMain(image.getProductId(), true)
                    .ifPresent(mainImg -> {
                        mainImg.setIsMain(false);
                        imageRepository.save(mainImg);
                    });
        }

        ProductImage savedImage = imageRepository.save(image);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);
    }

    // 3. DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProductImage(@PathVariable Long id) {
        if (imageRepository.existsById(id)) {
            imageRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    // El frontend envía el archivo como 'file' y el ID como 'productId'
    public ResponseEntity<ProductImage> uploadProductImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") Long productId) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String imageUrl;
        try {
            // 🔑 PASO CLAVE: Guardar el archivo localmente y obtener la URL pública real
            imageUrl = storeFile(file);
        } catch (IOException e) {
            System.err.println("Error al guardar el archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            System.err.println("Error al procesar el archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // 3. Crear el objeto ProductImage para persistir en DB.
        ProductImage newImage = new ProductImage();
        newImage.setProductId(productId);
        newImage.setImageUrl(imageUrl); // 🔑 Ahora contiene la URL completa (http://...)
        newImage.setSortOrder(99);

        // Lógica de imagen principal (isMain)
        long imageCount = imageRepository.countByProductId(productId);
        boolean isFirstImage = imageCount == 0;
        newImage.setIsMain(isFirstImage);

        if (!isFirstImage) {
            // Si ya existen imágenes, desmarcamos la principal anterior antes de guardar la nueva
            imageRepository.findByProductIdAndIsMain(productId, true)
                    .ifPresent(mainImg -> {
                        mainImg.setIsMain(false);
                        imageRepository.save(mainImg); // Guarda el cambio: isMain = false
                    });
        }

        ProductImage savedImage = imageRepository.save(newImage); // Guarda la nueva imagen

        return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);
    }

    // 🔑 3. ENDPOINT PARA URL EXTERNA
    @PostMapping("/add-url")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ProductImage> addProductImageFromUrl(@RequestBody ProductImage urlData) {
        // Asumimos que la URL en urlData.getImageUrl() ya es pública y válida.

        // Lógica de desmarcar y guardar
        if (urlData.getIsMain() != null && urlData.getIsMain()) {
            imageRepository.findByProductIdAndIsMain(urlData.getProductId(), true)
                    .ifPresent(mainImg -> {
                        mainImg.setIsMain(false);
                        imageRepository.save(mainImg);
                    });
        }

        ProductImage savedImage = imageRepository.save(urlData);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);
    }
}