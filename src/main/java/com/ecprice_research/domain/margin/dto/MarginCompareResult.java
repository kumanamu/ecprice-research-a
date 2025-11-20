package com.ecprice_research.domain.margin.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
public class MarginCompareResult {

    private String keyword;         // 검색어
    private String lang;            // 검색 언어 (ko/jp)

    // 각 플랫폼별 가격 정보
    private Map<String, PriceInfo> platformPrices;

    // 환율
    private double jpyToKrw;  // 1 JPY → KRW (만 있으면 충분함)

    // 분석 결과
    private String bestPlatform; // 최저가 플랫폼
    private long profitKrw;      // 최저가 KRW 수익
    private long profitJpy;      // 최저가 JPY 수익

    private AiMarginAnalysis aiAnalysis; // AI 분석 결과
}
