package com.ecprice_research.domain.margin.service;

import com.ecprice_research.domain.margin.dto.MarginCompareResult;
import com.ecprice_research.domain.margin.dto.PremiumMarginReportResponse;
import com.ecprice_research.domain.margin.util.PremiumPromptBuilder;
import com.ecprice_research.domain.openai.OpenAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PremiumMarginAnalysisService {

    private final OpenAiClient openAiClient;

    public PremiumMarginReportResponse analyze(MarginCompareResult result) {

        try {
            String prompt = PremiumPromptBuilder.build(result);
            String aiReport = openAiClient.ask(prompt);

            return PremiumMarginReportResponse.builder()
                    .keyword(result.getKeyword())
                    .bestPlatform(result.getBestPlatform())
                    .minPriceKrw(result.getProfitKrw())
                    .minPriceJpy(result.getProfitJpy())
                    .exchangeRate(result.getJpyToKrw())
                    .aiReport(aiReport)
                    .build();

        } catch (Exception e) {
            log.error("❌ Premium 분석 실패", e);

            return PremiumMarginReportResponse.builder()
                    .keyword(result.getKeyword())
                    .bestPlatform("-")
                    .minPriceKrw(0)
                    .minPriceJpy(0)
                    .exchangeRate(0)
                    .aiReport("AI 분석 실패")
                    .build();
        }
    }
}
