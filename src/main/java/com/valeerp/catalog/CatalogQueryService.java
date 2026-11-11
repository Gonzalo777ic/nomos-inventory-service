package com.valeerp.catalog;

import com.nomos.inventory.service.model.Product;
import com.nomos.inventory.service.repository.CategoryRepository;
import com.nomos.inventory.service.repository.ProductImageRepository;
import com.nomos.inventory.service.repository.ProductRepository;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CatalogQueryService {

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final CategoryRepository categoryRepository;

    public CatalogQueryService(ProductRepository productRepository,
                               ProductImageRepository imageRepository,
                               CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.categoryRepository = categoryRepository;
    }

    public CatalogProduct get(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new DomainException("El producto " + id + " no está en el catálogo"));
        return toView(product);
    }

    public Page<CatalogProduct> search(String search, int page, int size) {
        String term = search == null ? "" : search.trim();
        return productRepository.search(term, PageRequest.of(Math.max(page, 0), Math.max(size, 1)))
                .map(this::toView);
    }

    private CatalogProduct toView(Product product) {
        String image = imageRepository.findByProductIdAndMainTrue(product.getId())
                .map(picture -> picture.getImageUrl())
                .orElse(null);
        String category = categoryRepository.findById(product.getCategoryId())
                .map(item -> item.getName())
                .orElse(null);
        Money listPrice = product.getListPrice();
        return new CatalogProduct(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getProductType() == null ? null : product.getProductType().name(),
                product.getBarcode(),
                listPrice == null ? null : listPrice.amount(),
                listPrice == null ? null : listPrice.currencyCode(),
                image,
                category
        );
    }

    public record CatalogProduct(
            Long id,
            String name,
            String sku,
            String productType,
            String barcode,
            BigDecimal price,
            String currency,
            String imageUrl,
            String category
    ) {
    }
}
