package com.valeerp.procurement;

/**
 * Stub interno de asiento contable propuesto desde factura de proveedor.
 * Sin Feign a Contabilidad externa.
 */
public interface VendorBillAccountingPort {

    /**
     * @return referencia local del asiento stub propuesto
     */
    String proposeEntry(VendorBill bill);
}
