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
        try {
            // -----------------------------
            // ① 구매원가 계산 (KRW 기준)
            // -----------------------------
            double purchaseKrw = req.getPurchasePriceKrw();

            // JAPAN에서 구매가 제공되면 → KRW로 환산
            if (req.getPurchasePriceJpy() > 0) {
                purchaseKrw = req.getPurchasePriceJpy() * req.getExchangeRateJpyToKrw();
            }

            double baseCostKrw =
                    purchaseKrw
                            + req.getShippingLocal()
                            + req.getShippingInternational();

            // -----------------------------
            // ② 세금/수수료 계산
            // -----------------------------
            double taxKrw = purchaseKrw * req.getTaxRate();
            double platformFeeKrw = req.getSellPriceKrw() * req.getPlatformFee();

            // -----------------------------
            // ③ 총 비용 (Cost)
            // -----------------------------
            double totalCostKrw = baseCostKrw + taxKrw + platformFeeKrw;

            // -----------------------------
            // ④ 매출 (Revenue)
            // -----------------------------
            double revenueKrw = req.getSellPriceKrw();

            // JAPAN 판매가(JPY)가 있다면 → 환산 KRW와 비교 가능
            double revenueJpy = req.getSellPriceJpy();
            if (revenueJpy > 0) {
                revenueJpy = req.getSellPriceJpy();
            }

            // -----------------------------
            // ⑤ 순이익 (Profit)
            // -----------------------------
            double profitKrw = revenueKrw - totalCostKrw;
            double profitRateKrw = (totalCostKrw > 0)
                    ? (profitKrw / totalCostKrw) * 100
                    : 0;

            // JPY 기준 계산
            double totalCostJpy = totalCostKrw / req.getExchangeRateJpyToKrw();
            double profitJpy = revenueJpy - totalCostJpy;
            double profitRateJpy = (totalCostJpy > 0)
                    ? (profitJpy / totalCostJpy) * 100
                    : 0;

            // -----------------------------
            // ⑥ 결과 생성
            // -----------------------------
            return MarginSimulationResult.builder()
                    .totalCostKrw(totalCostKrw)
                    .totalCostJpy(totalCostJpy)
                    .revenueKrw(revenueKrw)
                    .revenueJpy(revenueJpy)
                    .profitKrw(profitKrw)
                    .profitJpy(profitJpy)
                    .profitRateKrw(profitRateKrw)
                    .profitRateJpy(profitRateJpy)
                    .build();

        } catch (Exception e) {
            log.error("마진 시뮬레이션 실패:", e);

            return MarginSimulationResult.builder()
                    .totalCostKrw(0)
                    .totalCostJpy(0)
                    .revenueKrw(0)
                    .revenueJpy(0)
                    .profitKrw(0)
                    .profitJpy(0)
                    .profitRateKrw(0)
                    .profitRateJpy(0)
                    .build();
        }
    }
}
