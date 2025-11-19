package com.ecprice_research.domain.margin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MarginCompareResult {

    private String keyword;    // 검색어
    private String lang;       // 언어 (ko/jp)

    private PriceInfo amazonJp;
    private PriceInfo rakuten;
    private PriceInfo naver;
    private PriceInfo coupang;

    private double krwToJpy;   // 1 KRW → JPY
    private long jpyToKrw;     // 1 JPY → KRW

    private String bestPlatform; // 최저가 플랫폼
    private long profitKrw;      // 최저가 KRW
    private long profitJpy;      // 최저가 JPY

    private AiMarginAnalysis aiAnalysis; // AI 분석 결과
}
