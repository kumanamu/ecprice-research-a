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

import java.util.*;

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

    // 기존 compare 유지
    public MarginCompareResult compare(String keyword, String lang) {
        return compare(keyword, lang, false);
    }

    // premium 포함 버전
    public MarginCompareResult compare(String keyword, String lang, boolean premium) {

        log.info("🔍 Margin Compare 실행: keyword={}, lang={}", keyword, lang);

        // 번역된 일본어 키워드
        String jp = translateService.koToJp(keyword);

        // 플랫폼 검색
        PriceInfo amazon = runSearch(keyword, jp, amazonService::search);
        PriceInfo rakuten = runSearch(keyword, jp, rakutenService::search);
        PriceInfo naver = naverService.search(keyword);
        PriceInfo coupang = coupangService.search(keyword);

        // Map 구성
        Map<String, PriceInfo> prices = new LinkedHashMap<>();
        prices.put("amazonJp", amazon);
        prices.put("rakuten", rakuten);
        prices.put("naver", naver);
        prices.put("coupang", coupang);

        // 환율
        ExchangeRate rate = exchangeService.getRate();
        int jpyToKrw = (int) rate.getJpyToKrw();
        double krwToJpy = rate.getKrwToJpy();

        // 가격 변환 (Integer 유지)
        for (PriceInfo pi : prices.values()) {
            if (pi == null || pi.getPriceOriginal() == null) continue;

            if ("JPY".equalsIgnoreCase(pi.getCurrencyOriginal())) {
                int krw = pi.getPriceOriginal() * jpyToKrw;
                pi.setPriceKrw(krw);
                pi.setPriceJpy(pi.getPriceOriginal());
            } else {
                int krw = pi.getPriceOriginal();
                int jpy = (int) (pi.getPriceOriginal() * krwToJpy);
                pi.setPriceKrw(krw);
                pi.setPriceJpy(jpy);
            }
        }
        MarginCompareResult result = MarginCompareResult.builder()
                .keyword(keyword)
                .lang(lang)
                .platformPrices(prices)
                .bestPlatform("-")
                .profitKrw(0)
                .profitJpy(0)
                .jpyToKrw(jpyToKrw)
                .build();

        // AI 분석 (기존 analyzeBasic 사용)
        AiMarginAnalysis basic = aiService.analyze(result, false);
        AiMarginAnalysis premiumAi = premium ? aiService.analyze(result, true) : null;

        return MarginCompareResult.builder()
                .keyword(keyword)
                .lang(lang)
                .platformPrices(prices)
                .bestPlatform("-")
                .profitKrw(0)
                .profitJpy(0)
                .jpyToKrw(jpyToKrw)
                .basicAi(basic)
                .premiumAi(premiumAi)
                .build();
    }

    // 키워드 후보 순차 검색
    private PriceInfo runSearch(String ko, String jp,
                                java.util.function.Function<String, PriceInfo> fn) {

        List<String> order = List.of(jp, ko);

        for (String key : order) {
            try {
                PriceInfo r = fn.apply(key);
                if (r != null && r.getPriceOriginal() != null && r.getPriceOriginal() > 0)
                    return r;
            } catch (Exception ignore) {}
        }

        return PriceInfo.builder()
                .platform("NONE")
                .productName("조회 실패")
                .priceOriginal(0)
                .currencyOriginal("KRW")
                .build();
    }
}
