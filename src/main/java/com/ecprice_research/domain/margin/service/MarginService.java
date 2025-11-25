package com.ecprice_research.domain.margin.service;

import com.ecprice_research.domain.keyword.engine.KeywordEngine;
import com.ecprice_research.keyword.engine.KeywordDetect;
import com.ecprice_research.domain.keyword.engine.PlatformRoutingEngine;
import com.ecprice_research.domain.margin.dto.AiMarginAnalysis;
import com.ecprice_research.domain.margin.dto.MarginCompareResult;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.exchange.dto.ExchangeRate;
import com.ecprice_research.domain.exchange.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarginService {

    private final KeywordEngine keywordEngine;
    private final PlatformRoutingEngine routing;
    private final ExchangeService exchangeService;
    private final OpenAiAnalysisService aiService;

    public MarginCompareResult compare(String keyword, String lang, boolean isPremium) {

        log.info("🔍 [MarginService] keyword='{}' toggle='{}'", keyword, lang);

        // 1) 언어 감지 + Variant 세트 생성
        KeywordDetect.LangType detected = KeywordDetect.detect(keyword);
        log.info("📘 입력 언어: {}", detected);

        var variants = keywordEngine.buildVariants(keyword);

        // 🔥 후보들 로그
        log.info("📦 Amazon 후보  = {}", variants.amazon());
        log.info("📦 Rakuten 후보 = {}", variants.rakuten());
        log.info("📦 Naver 후보   = {}", variants.naver());
        log.info("📦 Coupang 후보 = {}", variants.coupang());

        // 2) 플랫폼 검색
        Map<String, PriceInfo> prices = new HashMap<>();

        prices.put("amazonJp", routing.search("AMAZON", variants.amazon()));
        prices.put("rakuten", routing.search("RAKUTEN", variants.rakuten()));
        prices.put("naver", routing.search("NAVER", variants.naver()));
        prices.put("coupang", routing.search("COUPANG", variants.coupang()));

        // 3) 환율
        ExchangeRate rate = exchangeService.getRate();
        double jpyToKrw = rate.getJpyToKrw();
        double krwToJpy = rate.getKrwToJpy();

        // 4) KRW/JPY 변환
        for (PriceInfo pi : prices.values()) {

            if (pi == null || pi.getPriceOriginal() == null) continue;

            if ("JPY".equalsIgnoreCase(pi.getCurrencyOriginal())) {
                int jpy = pi.getPriceOriginal();
                int krw = (int) Math.round(jpy * jpyToKrw);

                pi.setPriceJpy(jpy);
                pi.setPriceKrw(krw);

            } else {
                int krw = pi.getPriceOriginal();
                int jpy = (int) Math.round(krw * krwToJpy);

                pi.setPriceKrw(krw);
                pi.setPriceJpy(jpy);
            }
        }

        // 5) 최저가 탐색
        String best = "-";
        int bestJpy = Integer.MAX_VALUE;

        for (var entry : prices.entrySet()) {
            PriceInfo pi = entry.getValue();

            if (pi == null || pi.getPriceJpy() == null) continue;

            if (pi.getPriceJpy() < bestJpy) {
                bestJpy = pi.getPriceJpy();
                best = entry.getKey();
            }
        }

        // 6) 기본 결과
        MarginCompareResult base = MarginCompareResult.builder()
                .keyword(keyword)
                .lang(lang)
                .platformPrices(prices)
                .bestPlatform(best)
                .profitKrw(0)
                .profitJpy(0)
                .jpyToKrw(jpyToKrw)
                .build();

        // 7) AI 분석
        AiMarginAnalysis basicAi = aiService.analyze(base, false);
        AiMarginAnalysis premiumAi = isPremium ? aiService.analyze(base, true) : null;

        return MarginCompareResult.builder()
                .keyword(keyword)
                .lang(lang)
                .platformPrices(prices)
                .bestPlatform(best)
                .profitKrw(0)
                .profitJpy(0)
                .jpyToKrw(jpyToKrw)
                .basicAi(basicAi)
                .premiumAi(premiumAi)
                .build();
    }
}
