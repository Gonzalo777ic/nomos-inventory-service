package com.nomos.inventory.service.model.dto;

public class UserAuthDTO {
    private Long id;
    private String username;
    private Long supplierId; 

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
}