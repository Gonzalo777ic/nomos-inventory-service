package com.valeerp.procurement;

import com.valeerp.kernel.SoftDeletable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "supplier_accounts")
public class SupplierAccount extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void link(Long userId, String username, Long supplierId) {
        this.userId = userId;
        this.username = username;
        this.supplierId = supplierId;
    }
}
