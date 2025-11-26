package com.ecprice_research.domain.margin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarginResponse {

    private String keyword;
    private String lang;

    private double jpyToKrw;
    private double krwToJpy;

    // 플랫폼별 결과
    private PriceInfo amazon;
    private PriceInfo rakuten;
    private PriceInfo naver;
    private PriceInfo coupang;

    // 최저가 정보
    private PriceInfo best;

    // Basic / Premium AI
    private AiMarginAnalysis basicAi;
    private AiMarginAnalysis premiumAi;
}
