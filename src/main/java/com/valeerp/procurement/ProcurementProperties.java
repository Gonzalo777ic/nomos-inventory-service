package com.valeerp.procurement;

import com.valeerp.kernel.Money;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Política de abastecimiento cargada desde entorno/configuración.
 */
@ConfigurationProperties(prefix = "valeerp.procurement")
public class ProcurementProperties {

    private int defaultLeadDays = 7;
    private BigDecimal approvalThresholdAmount = new BigDecimal("5000.00");
    private String approvalThresholdCurrency = "PEN";

    public int getDefaultLeadDays() {
        return defaultLeadDays;
    }

    public void setDefaultLeadDays(int defaultLeadDays) {
        this.defaultLeadDays = defaultLeadDays;
    }

    public BigDecimal getApprovalThresholdAmount() {
        return approvalThresholdAmount;
    }

    public void setApprovalThresholdAmount(BigDecimal approvalThresholdAmount) {
        this.approvalThresholdAmount = approvalThresholdAmount;
    }

    public String getApprovalThresholdCurrency() {
        return approvalThresholdCurrency;
    }

    public void setApprovalThresholdCurrency(String approvalThresholdCurrency) {
        this.approvalThresholdCurrency = approvalThresholdCurrency;
    }

    public Money approvalThreshold() {
        return Money.of(approvalThresholdAmount, approvalThresholdCurrency);
    }
}
