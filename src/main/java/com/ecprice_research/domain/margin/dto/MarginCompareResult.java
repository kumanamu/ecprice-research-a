package com.ecprice_research.domain.margin.dto;

import lombok.*;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarginCompareResult {

    private String keyword;
    private String lang;

    private Map<String, PriceInfo> platformPrices;

    private String bestPlatform;
    private long profitKrw;
    private long profitJpy;

    private double jpyToKrw;

    // 🔥 Premium / Basic AI 결과 저장 → 토글 즉시 출력 가능
    private AiMarginAnalysis basicAi;
    private AiMarginAnalysis premiumAi;
}
