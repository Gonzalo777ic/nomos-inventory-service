package com.valeerp.procurement;

import com.valeerp.kernel.Actors;
import com.valeerp.kernel.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class SupplierAccountService {

    private final SupplierAccountRepository repository;

    public SupplierAccountService(SupplierAccountRepository repository) {
        this.repository = repository;
    }

    public List<SupplierAccount> findAll() {
        return repository.findAll();
    }

    public Optional<Long> supplierIdOf(String username) {
        return repository.findByUsername(username).map(SupplierAccount::getSupplierId);
    }

    @Transactional
    public SupplierAccount link(Long userId, String username, Long supplierId) {
        if (userId == null || username == null || username.isBlank()) {
            throw new DomainException("La cuenta de abastecimiento exige usuario");
        }
        SupplierAccount account = repository.findAnyByUserId(userId).orElseGet(SupplierAccount::new);
        if (account.isRetired()) {
            account.restore();
        }
        if (supplierId == null) {
            if (account.getId() != null) {
                account.retire(Actors.current(), Instant.now());
                return repository.save(account);
            }
            throw new DomainException("El usuario no tiene una cuenta de proveedor para desvincular");
        }
        account.link(userId, username, supplierId);
        return repository.save(account);
    }
}
