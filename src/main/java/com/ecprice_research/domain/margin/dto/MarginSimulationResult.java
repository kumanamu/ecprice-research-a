package com.ecprice_research.domain.margin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarginSimulationResult {

    private double totalCostKrw;
    private double totalCostJpy;

    private double revenueKrw;
    private double revenueJpy;

    private double profitKrw;
    private double profitJpy;

    private double profitRateKrw;
    private double profitRateJpy;
}
