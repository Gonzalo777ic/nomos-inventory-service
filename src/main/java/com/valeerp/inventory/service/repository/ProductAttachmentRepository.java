package com.valeerp.inventory.service.repository;

import com.valeerp.inventory.service.model.ProductAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductAttachmentRepository extends JpaRepository<ProductAttachment, Long> {

    List<ProductAttachment> findByProductIdOrderByLabelAsc(Long productId);
}
