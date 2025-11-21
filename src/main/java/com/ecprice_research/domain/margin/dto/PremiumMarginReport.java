package com.ecprice_research.domain.margin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PremiumMarginReport {

    private String summary;          // 요약
    private String comparisonTable;  // 국가별 비교 도표
    private String logisticsFlow;    // 흐름도
    private String marketGraph;      // 그래프 느낌
    private String minSellPrice;     // 최소 판매가 도표
    private String strategyDetail;   // 상세 판매 전략
    private String finalConclusion;  // 최종 결론
}
