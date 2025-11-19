package com.ecprice_research.domain.margin.service;

import com.ecprice_research.domain.margin.dto.AiMarginAnalysis;
import com.ecprice_research.domain.margin.dto.MarginCompareResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiAnalysisService {

    public AiMarginAnalysis analyze(MarginCompareResult result) {
        try {

            String buy = result.getBestPlatform();
            String sell = "Amazon JP, Rakuten, Coupang";
            long profit = result.getProfitKrw();
            double rate = (profit > 0) ? 100.0 : 0.0;

            return AiMarginAnalysis.builder()
                    .buyPlatform(buy)
                    .sellPlatform(sell)
                    .profitKrw(profit)
                    .profitRate(rate)
                    .reason("자동 분석 결과 생성됨")
                    .build();

        } catch (Exception e) {
            log.error("AI 분석 실패:", e);

            return AiMarginAnalysis.builder()
                    .buyPlatform("-")
                    .sellPlatform("-")
                    .profitKrw(0)
                    .profitRate(0)
                    .reason("AI 분석 실패 (Fallback)")
                    .build();
        }
    }
}
