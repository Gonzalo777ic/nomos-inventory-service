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

public class ProductImageController {

    private final ProductImageRepository imageRepository;


    private final Path fileStorageLocation = Paths.get("uploads/images/").toAbsolutePath().normalize();


    private final String imagePublicPath = "/images/";

    private final String baseUrl;

    public ProductImageController(
            ProductImageRepository imageRepository,
            @Value("${server.base-url:http://localhost:8080}") String baseUrl) {

        this.imageRepository = imageRepository;
        this.baseUrl = baseUrl;

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {

            throw new RuntimeException("Error al inicializar el directorio de almacenamiento de imágenes.", ex);
        }
    }

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

        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        if (uniqueFilename.contains("..")) {
            throw new IOException("El nombre del archivo contiene una secuencia de ruta inválida.");
        }

        Path targetLocation = this.fileStorageLocation.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return this.baseUrl + this.imagePublicPath + uniqueFilename;
    }




    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_SUPPLIER')")
    public ResponseEntity<List<ProductImage>> getImagesByProduct(@PathVariable Long productId) {
        List<ProductImage> images = imageRepository.findByProductIdOrderBySortOrderAsc(productId);
        return ResponseEntity.ok(images);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ProductImage> addProductImage(@Valid @RequestBody ProductImage image) {

        if (image.getIsMain() != null && image.getIsMain()) {

            imageRepository.findByProductIdAndIsMain(image.getProductId(), true)
                    .ifPresent(mainImg -> {
                        mainImg.setIsMain(false);
                        imageRepository.save(mainImg);
                    });
        }

        ProductImage savedImage = imageRepository.save(image);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);
    }

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

    public ResponseEntity<ProductImage> uploadProductImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") Long productId) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String imageUrl;
        try {

            imageUrl = storeFile(file);
        } catch (IOException e) {
            System.err.println("Error al guardar el archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            System.err.println("Error al procesar el archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        ProductImage newImage = new ProductImage();
        newImage.setProductId(productId);
        newImage.setImageUrl(imageUrl); // 🔑 Ahora contiene la URL completa (http://...)
        newImage.setSortOrder(99);

        long imageCount = imageRepository.countByProductId(productId);
        boolean isFirstImage = imageCount == 0;
        newImage.setIsMain(isFirstImage);

        if (!isFirstImage) {

            imageRepository.findByProductIdAndIsMain(productId, true)
                    .ifPresent(mainImg -> {
                        mainImg.setIsMain(false);
                        imageRepository.save(mainImg); // Guarda el cambio: isMain = false
                    });
        }

        ProductImage savedImage = imageRepository.save(newImage); // Guarda la nueva imagen

        return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);
    }

    @PostMapping("/add-url")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ProductImage> addProductImageFromUrl(@RequestBody ProductImage urlData) {


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