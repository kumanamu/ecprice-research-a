package com.ecprice_research.domain.margin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MarginSimulationResult {

    private long totalCostKrw;
    private double totalCostJpy;

    private long revenueKrw;
    private double revenueJpy;

    private long profitKrw;
    private double profitJpy;

    private double profitRateKrw;   // 수익률 %
    private double profitRateJpy;   // 수익률 %
}
