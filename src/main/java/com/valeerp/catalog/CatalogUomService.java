package com.valeerp.catalog;

import com.nomos.inventory.service.model.UnitOfMeasure;
import com.nomos.inventory.service.model.UomConversion;
import com.nomos.inventory.service.repository.UnitOfMeasureRepository;
import com.nomos.inventory.service.repository.UomConversionRepository;
import com.valeerp.catalog.CatalogUomDtos.ConvertRequest;
import com.valeerp.catalog.CatalogUomDtos.ConvertResponse;
import com.valeerp.catalog.CatalogUomDtos.ConversionResponse;
import com.valeerp.catalog.CatalogUomDtos.CreateConversionRequest;
import com.valeerp.catalog.CatalogUomDtos.CreateUomRequest;
import com.valeerp.catalog.CatalogUomDtos.UpdateConversionRequest;
import com.valeerp.catalog.CatalogUomDtos.UpdateUomRequest;
import com.valeerp.catalog.CatalogUomDtos.UomResponse;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Unidades de medida y conversiones (caja↔unidad) como política de dominio.
 */
@Service
public class CatalogUomService {

    private final UnitOfMeasureRepository uomRepository;
    private final UomConversionRepository conversionRepository;
    private final Clock clock;

    public CatalogUomService(UnitOfMeasureRepository uomRepository,
                             UomConversionRepository conversionRepository,
                             Clock clock) {
        this.uomRepository = uomRepository;
        this.conversionRepository = conversionRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<UomResponse> listUnits() {
        return uomRepository.findAll().stream().map(this::toUomResponse).toList();
    }

    @Transactional(readOnly = true)
    public UomResponse getUnit(Long id) {
        return toUomResponse(requireUom(id));
    }

    @Transactional
    public UomResponse createUnit(CreateUomRequest request) {
        String abbreviation = request.abbreviation().trim().toUpperCase();
        if (uomRepository.existsByAbbreviation(abbreviation)) {
            throw new DomainException("Ya existe una UoM vigente con abreviatura " + abbreviation);
        }
        UnitOfMeasure uom = UnitOfMeasure.define(
                request.name(),
                abbreviation,
                UomCategory.parse(request.category())
        );
        return toUomResponse(uomRepository.save(uom));
    }

    @Transactional
    public UomResponse updateUnit(Long id, UpdateUomRequest request) {
        UnitOfMeasure uom = requireUom(id);
        String abbreviation = request.abbreviation().trim().toUpperCase();
        uomRepository.findByAbbreviation(abbreviation).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new DomainException("Ya existe otra UoM vigente con abreviatura " + abbreviation);
            }
        });
        uom.redefine(request.name(), abbreviation, UomCategory.parse(request.category()));
        return toUomResponse(uomRepository.save(uom));
    }

    @Transactional
    public void retireUnit(Long id) {
        UnitOfMeasure uom = requireUom(id);
        uom.retire(Actors.current(), Instant.now(clock));
        uomRepository.save(uom);
    }

    @Transactional(readOnly = true)
    public List<ConversionResponse> listConversions() {
        return conversionRepository.findAllByOrderByFromUomIdAsc().stream()
                .map(this::toConversionResponse)
                .toList();
    }

    @Transactional
    public ConversionResponse createConversion(CreateConversionRequest request) {
        UnitOfMeasure from = requireUom(request.fromUomId());
        UnitOfMeasure to = requireUom(request.toUomId());
        UomConversionPolicy.requireSameCategory(from, to);
        if (conversionRepository.existsByFromUomIdAndToUomId(from.getId(), to.getId())) {
            throw new DomainException("Ya existe una conversión entre esas unidades");
        }
        UomConversion conversion = UomConversion.define(
                from.getId(),
                to.getId(),
                ConversionFactor.of(request.factor())
        );
        return toConversionResponse(conversionRepository.save(conversion));
    }

    @Transactional
    public ConversionResponse updateConversion(Long id, UpdateConversionRequest request) {
        UomConversion conversion = requireConversion(id);
        conversion.redefine(ConversionFactor.of(request.factor()));
        return toConversionResponse(conversionRepository.save(conversion));
    }

    @Transactional
    public void retireConversion(Long id) {
        UomConversion conversion = requireConversion(id);
        conversion.retire(Actors.current(), Instant.now(clock));
        conversionRepository.save(conversion);
    }

    @Transactional(readOnly = true)
    public ConvertResponse convert(ConvertRequest request) {
        UnitOfMeasure from = requireUom(request.fromUomId());
        UnitOfMeasure to = requireUom(request.toUomId());
        UomConversion direct = conversionRepository
                .findByFromUomIdAndToUomId(from.getId(), to.getId())
                .orElse(null);
        UomConversion inverse = conversionRepository
                .findByFromUomIdAndToUomId(to.getId(), from.getId())
                .orElse(null);
        UomQuantity result = UomConversionPolicy.convert(
                UomQuantity.of(request.quantity(), from.getId()),
                from,
                to,
                direct,
                inverse
        );
        return new ConvertResponse(from.getId(), to.getId(), request.quantity(), result.amount());
    }

    private UomResponse toUomResponse(UnitOfMeasure uom) {
        return new UomResponse(uom.getId(), uom.getName(), uom.getAbbreviation(), uom.getCategory().name());
    }

    private ConversionResponse toConversionResponse(UomConversion conversion) {
        return new ConversionResponse(
                conversion.getId(),
                conversion.getFromUomId(),
                conversion.getToUomId(),
                conversion.getFactor()
        );
    }

    private UnitOfMeasure requireUom(Long id) {
        return uomRepository.findById(id)
                .orElseThrow(() -> new DomainException("La unidad de medida no existe"));
    }

    private UomConversion requireConversion(Long id) {
        return conversionRepository.findById(id)
                .orElseThrow(() -> new DomainException("La conversión no existe"));
    }
}
