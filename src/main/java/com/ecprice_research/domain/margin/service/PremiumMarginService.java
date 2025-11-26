package com.ecprice_research.domain.margin.service;

import com.ecprice_research.domain.margin.dto.*;
import com.ecprice_research.domain.openai.OpenAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PremiumMarginService {

    private final OpenAiClient ai;

    public AiMarginAnalysis buildAnalysis(
            PriceInfo a, PriceInfo r, PriceInfo n, PriceInfo c,
            PriceInfo best, MarginRequest req
    ) {
        if (best == null) return null;

        String prompt = """
                You are an e-commerce arbitrage expert.
                Analyze the following prices and determine the best buying/selling strategy.

                Amazon: %s KRW
                Rakuten: %s KRW
                Naver: %s KRW
                Coupang: %s KRW

                Best price: %s (%s)

                Return a short analysis in Korean/Japanese depending on "%s".
                """.formatted(
                safe(a.getPriceKrw()),
                safe(r.getPriceKrw()),
                safe(n.getPriceKrw()),
                safe(c.getPriceKrw()),
                best.getDisplayPrice(),
                best.getPlatform(),
                req.getLang()
        );

        String text = ai.ask(prompt);

        return AiMarginAnalysis.builder()
                .buyPlatform(best.getPlatform())
                .sellPlatform(req.getLang().equals("kr") ? "NAVER" : "AMAZON_JP")
                .profitKrw(best.getPriceKrw())
                .profitRate(0.0)
                .text(text)
                .reason("premium")
                .build();
    }

    private String safe(Integer v) {
        return (v == null) ? "N/A" : v.toString();
    }
}
