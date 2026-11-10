package com.valeerp.inventory;

import com.nomos.inventory.service.model.MovementType;
import com.valeerp.inventory.StockService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/integration/stock")
public class StockIntegrationController {

    private final StockService stockService;

    public StockIntegrationController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping("/issue")
    public void issue(@RequestBody StockCommand command) {
        MovementType type = MovementType.valueOf(command.type());
        for (StockLine line : command.lines()) {
            if (type == MovementType.AJUSTE_DEVOLUCION) {
                stockService.restock(line.productId(), line.quantity(), command.reason(),
                        command.referenceId(), command.referenceService());
            } else {
                stockService.issueByExpiry(line.productId(), line.quantity(), type, command.reason(),
                        command.referenceId(), command.referenceService());
            }
        }
    }

    @PostMapping("/receive")
    public void receive(@RequestBody StockCommand command) {
        for (StockLine line : command.lines()) {
            stockService.receive(line.productId(), line.warehouseId(), line.lotNumber(), line.quantity(),
                    line.unitCost(), command.referenceId(), command.referenceService(), command.reason());
        }
    }

    @PostMapping("/reverse")
    public void reverse(@RequestBody StockCommand command) {
        stockService.reverseReference(command.referenceId(), command.referenceService(), command.reason());
    }

    public record StockLine(Long productId, int quantity, Long warehouseId, String lotNumber,
                            java.math.BigDecimal unitCost) {
    }

    public record StockCommand(Long referenceId, String referenceService, String reason, String type,
                               List<StockLine> lines) {
    }
}
