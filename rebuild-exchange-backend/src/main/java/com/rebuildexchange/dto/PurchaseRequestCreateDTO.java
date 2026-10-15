package com.rebuildexchange.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PurchaseRequestCreateDTO {

    @NotNull(message = "Material ID is required")
    private Long materialId;

    @NotBlank(message = "Requested quantity is required")
    private String requestedQuantity;

    @DecimalMin(value = "0.0", inclusive = true, message = "Offered price cannot be negative")
    private BigDecimal offeredPrice;

    private String message;

    public PurchaseRequestCreateDTO() {}

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public String getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(String requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public BigDecimal getOfferedPrice() {
        return offeredPrice;
    }

    public void setOfferedPrice(BigDecimal offeredPrice) {
        this.offeredPrice = offeredPrice;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
