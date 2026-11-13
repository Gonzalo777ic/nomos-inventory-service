package com.valeerp.inventory.location;

import com.valeerp.inventory.service.repository.WarehouseRepository;
import com.valeerp.inventory.location.InternalLocationDtos.CreateInternalLocationRequest;
import com.valeerp.inventory.location.InternalLocationDtos.InternalLocationResponse;
import com.valeerp.inventory.location.InternalLocationDtos.UpdateInternalLocationRequest;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class InternalLocationService {

    private final InternalLocationRepository locationRepository;
    private final WarehouseRepository warehouseRepository;
    private final Clock clock;

    public InternalLocationService(InternalLocationRepository locationRepository,
                                   WarehouseRepository warehouseRepository,
                                   Clock clock) {
        this.locationRepository = locationRepository;
        this.warehouseRepository = warehouseRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<InternalLocationResponse> listByWarehouse(Long warehouseId) {
        requireWarehouse(warehouseId);
        return locationRepository.findByWarehouseIdOrderByLocationTypeAscCodeAsc(warehouseId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InternalLocationResponse get(Long id) {
        return toResponse(requireLocation(id));
    }

    @Transactional
    public InternalLocationResponse create(CreateInternalLocationRequest request) {
        requireWarehouse(request.warehouseId());
        String code = request.code().trim().toUpperCase();
        if (locationRepository.existsByWarehouseIdAndCode(request.warehouseId(), code)) {
            throw new DomainException("Ya existe una ubicación con código " + code + " en el almacén");
        }
        boolean asDefault = Boolean.TRUE.equals(request.defaultForType());
        if (asDefault) {
            clearDefault(request.warehouseId(), request.locationType(), null);
        }
        InternalLocation location = InternalLocation.open(
                request.warehouseId(), code, request.name(), request.locationType(), asDefault);
        return toResponse(locationRepository.save(location));
    }

    @Transactional
    public InternalLocationResponse update(Long id, UpdateInternalLocationRequest request) {
        InternalLocation location = requireLocation(id);
        String code = request.code().trim().toUpperCase();
        if (locationRepository.existsByWarehouseIdAndCodeAndIdNot(location.getWarehouseId(), code, id)) {
            throw new DomainException("Ya existe otra ubicación con código " + code + " en el almacén");
        }
        if (Boolean.TRUE.equals(request.defaultForType()) && !location.isDefaultForType()) {
            clearDefault(location.getWarehouseId(), location.getLocationType(), location.getId());
            location.markDefault();
        } else if (Boolean.FALSE.equals(request.defaultForType())) {
            location.clearDefault();
        }
        location.rename(code, request.name());
        return toResponse(locationRepository.save(location));
    }

    @Transactional
    public void retire(Long id) {
        InternalLocation location = requireLocation(id);
        location.retire(Actors.current(), Instant.now(clock));
        locationRepository.save(location);
    }

    /**
     * Crea las cuatro ubicaciones canónicas si el almacén aún no tiene ninguna.
     */
    @Transactional
    public void ensureDefaultsForWarehouse(Long warehouseId) {
        requireWarehouse(warehouseId);
        if (locationRepository.countByWarehouseId(warehouseId) > 0) {
            return;
        }
        for (InternalLocationType type : InternalLocationType.values()) {
            locationRepository.save(InternalLocation.defaultOf(warehouseId, type));
        }
    }

    @Transactional(readOnly = true)
    public InternalLocation requireDefault(Long warehouseId, InternalLocationType type) {
        return locationRepository
                .findByWarehouseIdAndLocationTypeAndDefaultForTypeTrue(warehouseId, type)
                .orElseThrow(() -> new DomainException(
                        "No hay ubicación por defecto " + type + " en el almacén " + warehouseId));
    }

    @Transactional(readOnly = true)
    public InternalLocation requireOwned(Long warehouseId, Long locationId) {
        InternalLocation location = requireLocation(locationId);
        if (!warehouseId.equals(location.getWarehouseId())) {
            throw new DomainException("La ubicación no pertenece al almacén " + warehouseId);
        }
        return location;
    }

    private void clearDefault(Long warehouseId, InternalLocationType type, Long exceptId) {
        locationRepository.findByWarehouseIdAndLocationTypeAndDefaultForTypeTrue(warehouseId, type)
                .ifPresent(current -> {
                    if (exceptId == null || !exceptId.equals(current.getId())) {
                        current.clearDefault();
                        locationRepository.save(current);
                    }
                });
    }

    private InternalLocation requireLocation(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new DomainException("La ubicación interna no existe"));
    }

    private void requireWarehouse(Long warehouseId) {
        if (warehouseId == null || !warehouseRepository.existsById(warehouseId)) {
            throw new DomainException("El almacén no existe");
        }
    }

    private InternalLocationResponse toResponse(InternalLocation location) {
        return new InternalLocationResponse(
                location.getId(),
                location.getWarehouseId(),
                location.getCode(),
                location.getName(),
                location.getLocationType(),
                location.isDefaultForType()
        );
    }
}
