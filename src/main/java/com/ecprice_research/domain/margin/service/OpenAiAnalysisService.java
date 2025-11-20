package com.ecprice_research.domain.margin.service;

import com.ecprice_research.domain.margin.dto.AiMarginAnalysis;
import com.ecprice_research.domain.margin.dto.MarginCompareResult;
import com.ecprice_research.domain.openai.OpenAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiAnalysisService {

    private final OpenAiClient openAiClient;

    public AiMarginAnalysis analyze(MarginCompareResult result) {

        try {
            String prompt = buildPrompt(result);
            String answer = openAiClient.ask(prompt);

            return AiMarginAnalysis.builder()
                    .buyPlatform(result.getBestPlatform())
                    .sellPlatform("Amazon JP / Rakuten / Coupang / Naver")
                    .profitKrw(result.getProfitKrw())
                    .profitRate(result.getProfitKrw() > 0 ? 100.0 : 0.0)
                    .reason(answer)
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

    private String buildPrompt(MarginCompareResult r) {
        return "상품 가격 비교 결과 기반으로 판매 전략을 분석해줘.\n\n"
                + "키워드: " + r.getKeyword() + "\n"
                + "최저가 플랫폼: " + r.getBestPlatform() + "\n"
                + "가격(KRW): " + r.getProfitKrw() + "\n\n"
                + "다음 내용을 포함해서 분석해줘:\n"
                + "- 어디서 사서 어디에 파는 게 이득인지\n"
                + "- 예상 마진 구조\n"
                + "- 리스크 요인\n"
                + "- 최종 추천 전략\n";
    }
}
