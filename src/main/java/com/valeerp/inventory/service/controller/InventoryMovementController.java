package com.valeerp.inventory.service.controller;

import com.valeerp.inventory.MovementQueryService;
import com.valeerp.inventory.StockDtos.CreateMovementRequest;
import com.valeerp.inventory.StockDtos.MovementResponse;
import com.valeerp.inventory.StockDtos.NamedLotMovementRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory-movements")
public class InventoryMovementController {

    private final MovementQueryService movementQueryService;

    public InventoryMovementController(MovementQueryService movementQueryService) {
        this.movementQueryService = movementQueryService;
    }

    @GetMapping
    public List<MovementResponse> getAllMovements() {
        return movementQueryService.listAll();
    }

    @GetMapping("/{id}")
    public MovementResponse getMovementById(@PathVariable Long id) {
        return movementQueryService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovementResponse createMovement(@Valid @RequestBody CreateMovementRequest request) {
        return movementQueryService.create(request);
    }

    @PostMapping("/receive")
    @ResponseStatus(HttpStatus.CREATED)
    public MovementResponse receive(@Valid @RequestBody NamedLotMovementRequest request) {
        return movementQueryService.receiveNamed(request);
    }

    @PostMapping("/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public MovementResponse issue(@Valid @RequestBody NamedLotMovementRequest request) {
        return movementQueryService.issueNamed(request);
    }

    @GetMapping("/product/{productId}")
    public List<MovementResponse> getMovementsByProduct(@PathVariable Long productId) {
        return movementQueryService.byProduct(productId);
    }
}
