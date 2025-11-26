package com.ecprice_research.domain.margin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor   // 🔥 PremiumMarginService에서 필요
@AllArgsConstructor
@Builder
public class AiMarginAnalysis {

    private String buyPlatform;
    private String sellPlatform;
    private long profitKrw;
    private double profitRate;

    private String text;      // 프론트에서 사용하는 필드
    private String reason;    // 내부 로깅/요약용

    public String summary() {
        return "Buy: " + buyPlatform
                + "\nSell: " + sellPlatform
                + "\nProfit: " + profitKrw + " KRW (" + profitRate + "%)"
                + "\nReason: " + reason;
    }
}
