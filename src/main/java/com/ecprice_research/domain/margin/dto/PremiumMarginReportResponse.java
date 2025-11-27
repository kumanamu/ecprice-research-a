package com.ecprice_research.domain.margin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PremiumMarginReportResponse {

    private String keyword;
    private String bestPlatform;

    private long minPriceKrw;
    private long minPriceJpy;

    private double exchangeRate;

    private String aiReport;  // AI가 생성한 고급 보고서 텍스트
}
