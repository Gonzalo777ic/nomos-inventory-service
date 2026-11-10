package com.valeerp.procurement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SupplierAccountRepository extends JpaRepository<SupplierAccount, Long> {

    Optional<SupplierAccount> findByUsername(String username);

    Optional<SupplierAccount> findByUserId(Long userId);

    @Query(value = "select * from supplier_accounts where user_id = ?1", nativeQuery = true)
    Optional<SupplierAccount> findAnyByUserId(Long userId);

    List<SupplierAccount> findBySupplierId(Long supplierId);
}
