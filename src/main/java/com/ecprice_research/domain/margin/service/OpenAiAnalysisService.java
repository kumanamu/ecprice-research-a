package com.ecprice_research.domain.margin.service;

import com.ecprice_research.domain.margin.dto.AiMarginAnalysis;
import com.ecprice_research.domain.margin.dto.MarginCompareResult;
import com.ecprice_research.domain.margin.util.BasicPromptBuilder;
import com.ecprice_research.domain.margin.util.PremiumPromptBuilder;
import com.ecprice_research.domain.openai.OpenAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiAnalysisService {

    private final OpenAiClient openAiClient;

    public AiMarginAnalysis analyze(MarginCompareResult result, boolean premium) {

        try {
            String prompt = premium
                    ? PremiumPromptBuilder.build(result)  // 고급 분석
                    : BasicPromptBuilder.build(result);   // 기본 분석

            String answer = openAiClient.ask(prompt);
            log.info("🤖 [AI Analysis] premium={} bestPlatform={}", premium, result.getBestPlatform());
            log.info("🤖 [AI Analysis Prompt] {}", prompt);

            return AiMarginAnalysis.builder()
                    .buyPlatform(result.getBestPlatform())
                    .sellPlatform("Amazon / Rakuten / Coupang / Naver")
                    .profitKrw(result.getProfitKrw())
                    .profitRate(result.getProfitKrw() > 0 ? 100.0 : 0.0)
                    .text(answer)      // 👈 프론트용
                    .reason(answer)    // 👈 내부요약용 (기존)
                    .build();

        } catch (Exception e) {
            log.error("AI 분석 실패", e);
            return AiMarginAnalysis.builder()
                    .buyPlatform("-")
                    .sellPlatform("-")
                    .profitKrw(0)
                    .profitRate(0)
                    .reason("AI 분석 실패")
                    .build();
        }
    }
}
