package com.ecprice_research.domain.margin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarginSimulationRequest {

    // 매입가 (기준 통화: KRW)
    private long purchasePriceKrw;

    // 일본에서 직접 구매한 경우 (환산용 선택 필드)
    private long purchasePriceJpy;

    // 배송비
    private long shippingLocal;            // 국내(from seller → warehouse)
    private long shippingInternational;    // 국제 (KR → JP or JP → KR)

    // 판매가 (기준 통화: KRW)
    private long sellPriceKrw;

    // 해외 판매가(JPY) → 필요하면 KRW 변환
    private long sellPriceJpy;

    // 수수료 및 세금
    private double platformFee;             // 판매 수수료 (0.1 = 10%)
    private double taxRate;                 // 관부가세율 (0.1 = 10%)

    // 환율 정보
    private double exchangeRateJpyToKrw;    // 1 JPY → KRW
}
