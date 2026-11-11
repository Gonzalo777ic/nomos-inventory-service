package com.valeerp.inventory.location;

import com.valeerp.inventory.location.InternalLocationDtos.CreateInternalLocationRequest;
import com.valeerp.inventory.location.InternalLocationDtos.InternalLocationResponse;
import com.valeerp.inventory.location.InternalLocationDtos.UpdateInternalLocationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory/internal-locations")
public class InternalLocationController {

    private final InternalLocationService locationService;

    public InternalLocationController(InternalLocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public List<InternalLocationResponse> list(@RequestParam Long warehouseId) {
        return locationService.listByWarehouse(warehouseId);
    }

    @GetMapping("/{id}")
    public InternalLocationResponse get(@PathVariable Long id) {
        return locationService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InternalLocationResponse create(@Valid @RequestBody CreateInternalLocationRequest request) {
        return locationService.create(request);
    }

    @PutMapping("/{id}")
    public InternalLocationResponse update(@PathVariable Long id,
                                           @Valid @RequestBody UpdateInternalLocationRequest request) {
        return locationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retire(@PathVariable Long id) {
        locationService.retire(id);
    }
}
