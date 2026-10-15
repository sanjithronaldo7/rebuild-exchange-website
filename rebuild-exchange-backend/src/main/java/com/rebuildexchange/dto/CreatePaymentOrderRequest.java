package com.rebuildexchange.dto;

import jakarta.validation.constraints.NotNull;

public class CreatePaymentOrderRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    public CreatePaymentOrderRequest() {}

    public CreatePaymentOrderRequest(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
