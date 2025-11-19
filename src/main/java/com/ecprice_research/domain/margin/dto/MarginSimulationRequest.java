package com.ecprice_research.domain.margin.dto;

import lombok.Data;

@Data
public class MarginSimulationRequest {
    private long purchasePriceKrw;
    private long purchasePriceJpy;
    private long shippingLocal;
    private long shippingInternational;
    private double platformFee;
    private long sellPriceKrw;
    private long sellPriceJpy;
    private double taxRate;
    private double exchangeRateJpyToKrw;
}
