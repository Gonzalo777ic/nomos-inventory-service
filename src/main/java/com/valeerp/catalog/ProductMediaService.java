package com.valeerp.catalog;

import com.valeerp.inventory.service.model.ProductAttachment;
import com.valeerp.inventory.service.model.ProductImage;
import com.valeerp.inventory.service.repository.ProductAttachmentRepository;
import com.valeerp.inventory.service.repository.ProductImageRepository;
import com.valeerp.inventory.service.repository.ProductRepository;
import com.valeerp.catalog.ProductMediaDtos.AttachFileRequest;
import com.valeerp.catalog.ProductMediaDtos.AttachImageRequest;
import com.valeerp.catalog.ProductMediaDtos.AttachmentResponse;
import com.valeerp.catalog.ProductMediaDtos.ImageResponse;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class ProductMediaService {

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final ProductAttachmentRepository attachmentRepository;
    private final Clock clock;

    public ProductMediaService(ProductRepository productRepository,
                               ProductImageRepository imageRepository,
                               ProductAttachmentRepository attachmentRepository,
                               Clock clock) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.attachmentRepository = attachmentRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ImageResponse> listImages(Long productId) {
        requireProduct(productId);
        return imageRepository.findByProductIdOrderBySortOrderAsc(productId).stream()
                .map(this::toImageResponse)
                .toList();
    }

    @Transactional
    public ImageResponse attachImage(AttachImageRequest request) {
        requireProduct(request.productId());
        boolean main = Boolean.TRUE.equals(request.main())
                || imageRepository.countByProductId(request.productId()) == 0;
        if (main) {
            clearMain(request.productId());
        }
        ProductImage image = ProductImage.attach(
                request.productId(),
                request.variantId(),
                request.imageUrl(),
                request.storagePath(),
                request.contentType(),
                request.sizeBytes(),
                request.fileName(),
                main,
                request.sortOrder()
        );
        return toImageResponse(imageRepository.save(image));
    }

    @Transactional
    public void retireImage(Long imageId) {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new DomainException("Imagen no encontrada: " + imageId));
        image.retire(Actors.current(), Instant.now(clock));
        imageRepository.save(image);
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> listAttachments(Long productId) {
        requireProduct(productId);
        return attachmentRepository.findByProductIdOrderByLabelAsc(productId).stream()
                .map(this::toAttachmentResponse)
                .toList();
    }

    @Transactional
    public AttachmentResponse attachFile(AttachFileRequest request) {
        requireProduct(request.productId());
        ProductAttachment attachment = ProductAttachment.attach(
                request.productId(),
                request.variantId(),
                request.label(),
                request.url(),
                request.storagePath(),
                request.contentType(),
                request.sizeBytes(),
                request.fileName()
        );
        return toAttachmentResponse(attachmentRepository.save(attachment));
    }

    @Transactional
    public void retireAttachment(Long attachmentId) {
        ProductAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new DomainException("Adjunto no encontrado: " + attachmentId));
        attachment.retire(Actors.current(), Instant.now(clock));
        attachmentRepository.save(attachment);
    }

    private void clearMain(Long productId) {
        imageRepository.findByProductIdAndMainTrue(productId).ifPresent(current -> {
            current.clearMain();
            imageRepository.save(current);
        });
    }

    private void requireProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new DomainException("Producto no encontrado: " + productId);
        }
    }

    private ImageResponse toImageResponse(ProductImage image) {
        return new ImageResponse(
                image.getId(),
                image.getProductId(),
                image.getVariantId(),
                image.getImageUrl(),
                image.getStoragePath(),
                image.getContentType(),
                image.getSizeBytes(),
                image.getFileName(),
                image.isMain(),
                image.getSortOrder()
        );
    }

    private AttachmentResponse toAttachmentResponse(ProductAttachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getProductId(),
                attachment.getVariantId(),
                attachment.getLabel(),
                attachment.getUrl(),
                attachment.getStoragePath(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getFileName()
        );
    }
}
