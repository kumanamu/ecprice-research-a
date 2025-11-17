package com.ecprice_research.domain.margin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MarginSimulationRequest {

    private long purchasePriceKrw;          // 구입가 (KRW)
    private long purchasePriceJpy;          // 구입가 (JPY)

    private long shippingLocal;             // 현지 배송비
    private long shippingInternational;     // 해외 배송비(배대지 포함)

    private double platformFee;             // 판매 플랫폼 수수료 %

    private long sellPriceKrw;              // 판매가 KRW
    private long sellPriceJpy;              // 판매가 JPY

    private double taxRate;                 // 관세 / 부가세 %
    private double exchangeRateJpyToKrw;    // 환율: 1JPY → KRW
}
