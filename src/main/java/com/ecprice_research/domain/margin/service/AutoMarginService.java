package com.ecprice_research.domain.margin.service;

import com.ecprice_research.domain.margin.dto.AutoMarginResponse;
import com.ecprice_research.domain.margin.dto.MarginCompareResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AutoMarginService {

    public AutoMarginResponse compute(MarginCompareResult result) {

        if (result.getProfitKrw() <= 0) {
            return AutoMarginResponse.builder()
                    .autoBuyPrice(0)
                    .autoSellPrice(0)
                    .autoProfit(0)
                    .autoProfitRate(0)
                    .aiStrategy("수익이 발생하지 않아 자동 추천 불가")
                    .build();
        }

        long buy = result.getProfitKrw();
        long sell = (long) (buy * 1.25);     // 기본 판매가: 매입 대비 25% 증가
        long profit = sell - buy;
        double profitRate = (double) profit / buy * 100;

        return AutoMarginResponse.builder()
                .autoBuyPrice(buy)
                .autoSellPrice(sell)
                .autoProfit(profit)
                .autoProfitRate(profitRate)
                .build();
    }
}
