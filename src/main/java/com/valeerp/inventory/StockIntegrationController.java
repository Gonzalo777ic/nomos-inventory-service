package com.valeerp.inventory;

import com.valeerp.inventory.service.model.MovementType;
import com.valeerp.kernel.Currency;
import com.valeerp.kernel.DomainException;
import com.valeerp.kernel.Money;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
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
        OperationType type = resolveOperation(command.type());
        for (StockLine line : command.lines()) {
            requireNamedLot(line);
            stockService.issueNamedLot(
                    line.productId(),
                    line.warehouseId(),
                    line.locationId(),
                    line.lotNumber(),
                    line.serialNumber(),
                    line.quantity(),
                    type,
                    command.reason(),
                    command.referenceId(),
                    command.referenceService()
            );
        }
    }

    @PostMapping("/receive")
    public void receive(@RequestBody StockCommand command) {
        OperationType type = command.type() == null || command.type().isBlank()
                ? OperationType.RECEIPT
                : resolveOperation(command.type());
        for (StockLine line : command.lines()) {
            requireNamedLot(line);
            Money cost = line.unitCost() == null
                    ? Money.zero(Currency.PEN)
                    : Money.pen(line.unitCost());
            stockService.receive(
                    line.productId(),
                    line.warehouseId(),
                    line.locationId(),
                    line.lotNumber(),
                    line.serialNumber(),
                    line.quantity(),
                    cost,
                    type,
                    command.referenceId(),
                    command.referenceService(),
                    command.reason()
            );
        }
    }

    @PostMapping("/reverse")
    public void reverse(@RequestBody StockCommand command) {
        stockService.reverseReference(command.referenceId(), command.referenceService(), command.reason());
    }

    private static void requireNamedLot(StockLine line) {
        if (line.warehouseId() == null) {
            throw new DomainException("Cada línea exige warehouseId");
        }
        if (line.lotNumber() == null || line.lotNumber().isBlank()) {
            throw new DomainException("Cada línea exige lotNumber; no se elige un lote al azar");
        }
    }

    private static OperationType resolveOperation(String token) {
        return MovementType.fromToken(token).toOperationType();
    }

    public record StockLine(Long productId, int quantity, Long warehouseId, Long locationId,
                            String lotNumber, String serialNumber, BigDecimal unitCost) {
    }

    public record StockCommand(Long referenceId, String referenceService, String reason, String type,
                               List<StockLine> lines) {
    }
}
