package com.ecprice_research.domain.margin.service;

import com.ecprice_research.domain.amazon.service.AmazonService;
import com.ecprice_research.domain.coupang.service.CoupangService;
import com.ecprice_research.domain.exchange.dto.ExchangeRate;
import com.ecprice_research.domain.exchange.service.ExchangeService;
import com.ecprice_research.domain.margin.dto.AiMarginAnalysis;
import com.ecprice_research.domain.margin.dto.MarginCompareResult;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.naver.service.NaverService;
import com.ecprice_research.domain.rakuten.service.RakutenService;
import com.ecprice_research.domain.translate.service.TranslateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarginService {

    private final AmazonService amazonService;
    private final RakutenService rakutenService;
    private final NaverService naverService;
    private final CoupangService coupangService;

    // 🔥 안정 버전 TranslateService (gpt-4o-mini)
    private final TranslateService translateService;

    private final ExchangeService exchangeService;
    private final OpenAiAnalysisService aiService;


    // ======================================================
    // 🔥 Basic + Premium AI 분석을 한 번에 생성
    // ======================================================
    public MarginCompareResult compare(String keyword, String lang) {

        log.info("🔍 [Margin Compare] keyword='{}', lang='{}'", keyword, lang);

        // 1) 입력 언어 감지
        String detected = detectLanguage(keyword);

        // 2) 검색 키워드 변환
        SearchKeywords keys = convertKeyword(keyword, detected);

        // 3) 플랫폼별 가격 조회
        PriceInfo amazon  = amazonService.search(keys.amazon());
        PriceInfo rakuten = rakutenService.search(keys.rakuten());
        PriceInfo naver   = naverService.search(keys.naver());
        PriceInfo coupang = coupangService.search(keys.coupang());

        Map<String, PriceInfo> prices = new LinkedHashMap<>();
        prices.put("amazonJp", amazon);
        prices.put("rakuten", rakuten);
        prices.put("naver", naver);
        prices.put("coupang", coupang);

        // 4) 환율 조회 (캐싱)
        ExchangeRate rate = exchangeService.getRate();
        double jpyToKrw = rate.getJpyToKrw();
        double krwToJpy = rate.getKrwToJpy();

        // 5) KRW/JPY 통일 변환
        for (PriceInfo p : prices.values()) {
            if (p == null || p.getPriceOriginal() == null) continue;

            if ("JPY".equalsIgnoreCase(p.getCurrencyOriginal())) {
                int jpy = p.getPriceOriginal();
                p.setPriceJpy(jpy);
                p.setPriceKrw((int) Math.round(jpy * jpyToKrw));
            } else {
                int krw = p.getPriceOriginal();
                p.setPriceKrw(krw);
                p.setPriceJpy((int) Math.round(krw * krwToJpy));
            }
        }

        // 6) 최저가 플랫폼 선정
        String bestPlatform = "-";
        int minJpy = Integer.MAX_VALUE;

        for (var entry : prices.entrySet()) {
            PriceInfo p = entry.getValue();

            if (p != null && p.getPriceJpy() != null && p.getPriceJpy() > 0) {
                if (p.getPriceJpy() < minJpy) {
                    minJpy = p.getPriceJpy();
                    bestPlatform = entry.getKey();
                }
            }
        }

        // 7) 기본 DTO 구축
        MarginCompareResult base = MarginCompareResult.builder()
                .keyword(keyword)
                .lang(lang)
                .platformPrices(prices)
                .bestPlatform(bestPlatform)
                .profitKrw(0)
                .profitJpy(0)
                .jpyToKrw((int) jpyToKrw)
                .build();

        // 8) Basic + Premium AI 분석 생성
        AiMarginAnalysis basicAi   = aiService.analyze(base, false);
        AiMarginAnalysis premiumAi = aiService.analyze(base, true);

        // 9) 최종 반환
        return MarginCompareResult.builder()
                .keyword(keyword)
                .lang(lang)
                .platformPrices(prices)
                .bestPlatform(bestPlatform)
                .profitKrw(0)
                .profitJpy(0)
                .jpyToKrw((int) jpyToKrw)
                .basicAi(basicAi)
                .premiumAi(premiumAi)
                .build();
    }



    // ======================================================
    // 🔎 입력 언어 감지 (기존 유지)
    // ======================================================
    private String detectLanguage(String text) {
        if (text.matches(".*[가-힣].*")) return "KR";
        if (text.matches(".*[ぁ-んァ-ン一-龥].*")) return "JP";
        return "EN";
    }

    // ======================================================
    // 🔥 gpt-4o-mini 기반 번역
    // ======================================================
    private SearchKeywords convertKeyword(String keyword, String detected) {

        String jp;
        String kr;

        switch (detected) {
            case "KR" -> {
                kr = keyword;
                jp = translateService.koToJp(keyword);
            }
            case "JP" -> {
                jp = keyword;
                kr = translateService.jpToKo(keyword);
            }
            default -> { // EN
                jp = keyword;
                kr = keyword;
            }
        }

        return new SearchKeywords(jp, jp, kr, kr);
    }

    private record SearchKeywords(String amazon, String rakuten, String naver, String coupang) {}
}
