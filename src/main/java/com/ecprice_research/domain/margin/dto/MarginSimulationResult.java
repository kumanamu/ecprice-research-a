package com.ecprice_research.domain.margin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarginSimulationResult {

    // 총 비용
    private double totalCostKrw;
    private double totalCostJpy;

    // 총 매출
    private double revenueKrw;
    private double revenueJpy;

    // 순이익
    private double profitKrw;
    private double profitJpy;

    // 수익률
    private double profitRateKrw;
    private double profitRateJpy;
}
