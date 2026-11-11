package com.valeerp.inventory;

import com.valeerp.inventory.location.InternalLocationType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/inventory/operation-types")
public class OperationTypeController {

    @GetMapping
    public List<OperationTypeView> list() {
        return Arrays.stream(OperationType.values())
                .map(type -> new OperationTypeView(
                        type.name(),
                        OperationTypePolicy.defaultSource(type),
                        OperationTypePolicy.defaultDestination(type)
                ))
                .toList();
    }

    public record OperationTypeView(
            String code,
            InternalLocationType defaultSource,
            InternalLocationType defaultDestination
    ) {
    }
}
