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

    private final TranslateService translateService;
    private final ExchangeService exchangeService;
    private final OpenAiAnalysisService aiService;

    public MarginCompareResult compare(String keyword, String toggle) {
        return compare(keyword, toggle, false);
    }

    public MarginCompareResult compare(String keyword, String toggle, boolean premium) {

        log.info("🔍 [Margin Compare] keyword={}, toggle={}", keyword, toggle);

        // 1) 입력 언어 감지
        String detected = detectLanguage(keyword);
        log.info("📘 입력 언어: {}", detected);

        // 2) 검색 키워드 변환
        SearchKeywords keys = convertKeyword(keyword, detected, toggle);

        // 3) 플랫폼별 검색
        PriceInfo amazon = amazonService.search(keys.amazon());
        PriceInfo rakuten = rakutenService.search(keys.rakuten());
        PriceInfo naver = naverService.search(keys.naver());
        PriceInfo coupang = coupangService.search(keys.coupang());

        Map<String, PriceInfo> prices = new LinkedHashMap<>();
        prices.put("amazonJp", amazon);
        prices.put("rakuten", rakuten);
        prices.put("naver", naver);
        prices.put("coupang", coupang);

        // 4) 환율
        ExchangeRate rate = exchangeService.getRate();
        double jpyToKrw = rate.getJpyToKrw();
        double krwToJpy = rate.getKrwToJpy();

        // 5) 통일된 KRW/JPY 저장
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

        // 6) 최저가격 플랫폼 선정
        String bestPlatform = "-";
        int minJpy = Integer.MAX_VALUE;

        for (Map.Entry<String, PriceInfo> entry : prices.entrySet()) {
            PriceInfo pi = entry.getValue();
            if (pi == null || pi.getPriceJpy() == null) continue;

            if (pi.getPriceJpy() < minJpy && pi.getPriceJpy() > 0) {
                minJpy = pi.getPriceJpy();
                bestPlatform = entry.getKey();
            }
        }

        // 7) 결과 기본 객체 구성
        MarginCompareResult base = MarginCompareResult.builder()
                .keyword(keyword)
                .lang(toggle)
                .platformPrices(prices)
                .bestPlatform(bestPlatform)
                .profitKrw(0)
                .profitJpy(0)
                .jpyToKrw((int) jpyToKrw)
                .build();

        // 8) AI 분석 (Basic + Premium)
        AiMarginAnalysis basicAi = aiService.analyze(base, false);
        AiMarginAnalysis premiumAi = premium ? aiService.analyze(base, true) : null;

        return MarginCompareResult.builder()
                .keyword(keyword)
                .lang(toggle)
                .platformPrices(prices)
                .bestPlatform(bestPlatform)
                .profitKrw(0)
                .profitJpy(0)
                .jpyToKrw((int) jpyToKrw)
                .basicAi(basicAi)
                .premiumAi(premiumAi)
                .build();
    }


    // ─────────────────────────────────────────────
    // 입력 언어 감지 (KR / JP / EN)
    // ─────────────────────────────────────────────
    private String detectLanguage(String text) {
        if (text.matches(".*[가-힣].*")) return "KR";
        if (text.matches(".*[ぁ-んァ-ン一-龥].*")) return "JP";
        return "EN";
    }


    // ─────────────────────────────────────────────
    // 11번 규칙 100% 적용: 검색어 변환
    // ─────────────────────────────────────────────
    private SearchKeywords convertKeyword(String keyword, String detected, String toggle) {

        String jp;
        String kr;

        // EN 입력 → 번역 금지 RAW
        if ("EN".equals(detected)) {
            jp = keyword;
            kr = keyword;
        }
        // KR 입력
        else if ("KR".equals(detected)) {
            jp = translateService.koToJp(keyword);
            kr = keyword;
        }
        // JP 입력
        else {
            jp = keyword;
            kr = translateService.jpToKo(keyword);
        }

        // 토글은 “출력 언어” 결정용
        // 검색어는 Amazon/Rakuten → jp, Naver/Coupang → kr
        return new SearchKeywords(
                jp,  // Amazon
                jp,  // Rakuten
                kr,  // Naver
                kr   // Coupang
        );
    }

    // 내부 record
    private record SearchKeywords(String amazon, String rakuten, String naver, String coupang) {}
}
