package com.valeerp.inventory.stock;

import com.valeerp.inventory.StockDtos.LotResponse;
import com.valeerp.inventory.StockDtos.SerialResponse;
import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class StockLotController {

    private final StockLotRepository lotRepository;
    private final StockSerialRepository serialRepository;
    private final Clock clock;

    public StockLotController(StockLotRepository lotRepository,
                              StockSerialRepository serialRepository,
                              Clock clock) {
        this.lotRepository = lotRepository;
        this.serialRepository = serialRepository;
        this.clock = clock;
    }

    @GetMapping("/lots")
    public List<LotResponse> lots(@RequestParam Long productId) {
        return lotRepository.findByProductIdOrderByLotNumberAsc(productId).stream()
                .map(lot -> new LotResponse(lot.getId(), lot.getProductId(), lot.getLotNumber(), lot.getExpirationDate()))
                .toList();
    }

    @GetMapping("/serials")
    public List<SerialResponse> serials(@RequestParam Long productId) {
        return serialRepository.findByProductIdOrderBySerialNumberAsc(productId).stream()
                .map(s -> new SerialResponse(s.getId(), s.getProductId(), s.getLotId(), s.getSerialNumber()))
                .toList();
    }

    @DeleteMapping("/lots/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retireLot(@PathVariable Long id) {
        StockLot lot = lotRepository.findById(id)
                .orElseThrow(() -> new DomainException("El lote no existe"));
        lot.retire(Actors.current(), Instant.now(clock));
        lotRepository.save(lot);
    }

    @DeleteMapping("/serials/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retireSerial(@PathVariable Long id) {
        StockSerial serial = serialRepository.findById(id)
                .orElseThrow(() -> new DomainException("La serie no existe"));
        serial.retire(Actors.current(), Instant.now(clock));
        serialRepository.save(serial);
    }
}
