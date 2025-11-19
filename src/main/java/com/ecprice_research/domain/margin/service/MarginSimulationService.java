package com.ecprice_research.domain.margin.service;

import com.ecprice_research.domain.margin.dto.MarginSimulationRequest;
import com.ecprice_research.domain.margin.dto.MarginSimulationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarginSimulationService {

    public MarginSimulationResult simulate(MarginSimulationRequest req) {

        long baseCost = req.getPurchasePriceKrw()
                + req.getShippingLocal()
                + req.getShippingInternational();

        long tax = Math.round(req.getPurchasePriceKrw() * req.getTaxRate());
        long totalCostKrw = baseCost + tax;

        long revenueKrw = req.getSellPriceKrw();

        long profitKrw = revenueKrw - totalCostKrw;
        double profitRate = (totalCostKrw > 0)
                ? ((double) profitKrw / totalCostKrw) * 100
                : 0.0;

        return MarginSimulationResult.builder()
                .totalCostKrw(totalCostKrw)
                .revenueKrw(revenueKrw)
                .profitKrw(profitKrw)
                .profitRateKrw(profitRate)
                .build();
    }
}
