package com.ecprice_research.domain.margin.service;

import com.ecprice_research.domain.margin.dto.MarginSimulationRequest;
import com.ecprice_research.domain.margin.dto.MarginSimulationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarginSimulationService {

    public MarginSimulationResult simulate(MarginSimulationRequest req) {

        // ❗ 총 비용 (KRW 기준)
        long totalCostKrw =
                req.getPurchasePriceKrw()
                        + req.getShippingLocal()
                        + req.getShippingInternational()
                        + (long)(req.getPurchasePriceKrw() * req.getTaxRate());

        // ❗ 총 비용 (JPY 기준)
        double totalCostJpy =
                req.getPurchasePriceJpy()
                        + (req.getShippingLocal() / req.getExchangeRateJpyToKrw())
                        + (req.getShippingInternational() / req.getExchangeRateJpyToKrw())
                        + (req.getPurchasePriceJpy() * req.getTaxRate());

        // ❗ 수익 (판매가 - 플랫폼 수수료)
        long revenueKrw = (long)(req.getSellPriceKrw() * (1 - req.getPlatformFee()));
        double revenueJpy = req.getSellPriceJpy() * (1 - req.getPlatformFee());

        // ❗ 마진
        long profitKrw = revenueKrw - totalCostKrw;
        double profitJpy = revenueJpy - totalCostJpy;

        return MarginSimulationResult.builder()
                .totalCostKrw(totalCostKrw)
                .totalCostJpy(totalCostJpy)

                .revenueKrw(revenueKrw)
                .revenueJpy(revenueJpy)

                .profitKrw(profitKrw)
                .profitJpy(profitJpy)

                .profitRateKrw(((double)profitKrw / totalCostKrw) * 100)
                .profitRateJpy((profitJpy / totalCostJpy) * 100)
                .build();
    }
}
