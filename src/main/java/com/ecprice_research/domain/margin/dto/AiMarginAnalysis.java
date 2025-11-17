package com.ecprice_research.domain.margin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiMarginAnalysis {
    private String buyRecommendation;
    private String sellRecommendation;
    private long expectedProfitKrw;
    private double expectedProfitRate;
    private String reason;
}
