package com.valeerp.catalog;

import com.nomos.inventory.service.model.Brand;
import com.nomos.inventory.service.repository.BrandRepository;
import com.valeerp.catalog.BrandDtos.BrandResponse;
import com.valeerp.catalog.BrandDtos.CreateBrandRequest;
import com.valeerp.catalog.BrandDtos.UpdateBrandRequest;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class BrandService {

    private final BrandRepository brandRepository;
    private final Clock clock;

    public BrandService(BrandRepository brandRepository, Clock clock) {
        this.brandRepository = brandRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> listAll() {
        return brandRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BrandResponse get(Long id) {
        return toResponse(require(id));
    }

    @Transactional
    public BrandResponse create(CreateBrandRequest request) {
        if (brandRepository.existsByName(request.name().trim())) {
            throw new DomainException("Ya existe una marca con el nombre " + request.name());
        }
        if (brandRepository.existsByCode(request.code().trim().toUpperCase())) {
            throw new DomainException("Ya existe una marca con el código " + request.code());
        }
        Brand brand = Brand.register(request.name(), request.code(), request.website(), request.logoUrl());
        return toResponse(brandRepository.save(brand));
    }

    @Transactional
    public BrandResponse update(Long id, UpdateBrandRequest request) {
        Brand brand = require(id);
        if (brandRepository.existsByNameAndIdNot(request.name().trim(), id)) {
            throw new DomainException("Ya existe otra marca con el nombre " + request.name());
        }
        if (brandRepository.existsByCodeAndIdNot(request.code().trim().toUpperCase(), id)) {
            throw new DomainException("Ya existe otra marca con el código " + request.code());
        }
        brand.rename(request.name(), request.code(), request.website(), request.logoUrl());
        return toResponse(brandRepository.save(brand));
    }

    @Transactional
    public void retire(Long id) {
        Brand brand = require(id);
        brand.retire(Actors.current(), Instant.now(clock));
        brandRepository.save(brand);
    }

    private Brand require(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new DomainException("Marca no encontrada: " + id));
    }

    private BrandResponse toResponse(Brand brand) {
        return new BrandResponse(brand.getId(), brand.getName(), brand.getCode(),
                brand.getWebsite(), brand.getLogoUrl());
    }
}
