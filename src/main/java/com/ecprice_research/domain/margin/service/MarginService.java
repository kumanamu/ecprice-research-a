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
import java.util.concurrent.*;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarginService {

    private final AmazonService amazonService;
    private final RakutenService rakutenService;
    private final NaverService naverService;
    private final CoupangService coupangService;

    private final OpenAiAnalysisService aiService;
    private final TranslateService translateService;
    private final ExchangeService exchangeService;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private static final int TIMEOUT_SEC = 25;

    // =====================================================================
    // 🔥 메인 비교 엔트리포인트
    // =====================================================================
    public MarginCompareResult compare(String keyword, String lang) {

        log.info("🔍 Margin Compare 실행: keyword={}, lang={}", keyword, lang);

        boolean isEnglish = keyword.matches("^[a-zA-Z0-9\\s\\-_.]+$");

        // =====================================================================
        // 🔥 STEP 1 — 검색 후보 생성
        // =====================================================================
        List<String> amazonCandidates;
        List<String> rakutenCandidates;
        List<String> naverCandidates;
        List<String> coupangCandidates;

        if (isEnglish) {
            amazonCandidates = List.of(keyword);
            rakutenCandidates = List.of(keyword);
            naverCandidates = List.of(keyword);
            coupangCandidates = List.of(keyword);

        } else if (lang.equals("ko")) {
            String jp = translateService.koToJp(keyword);

            amazonCandidates = List.of(jp, keyword);
            rakutenCandidates = List.of(jp);
            naverCandidates = List.of(keyword);
            coupangCandidates = List.of(keyword);

        } else {
            String ko = translateService.jpToKo(keyword);

            amazonCandidates = List.of(keyword);
            rakutenCandidates = List.of(keyword);
            naverCandidates = List.of(ko);
            coupangCandidates = List.of(ko);
        }

        // =====================================================================
        // 🔥 STEP 2 — 병렬 검색 수행
        // =====================================================================
        Map<String, PriceInfo> prices = new LinkedHashMap<>();
        prices.put("amazonJp", runCandidates("AMAZON_JP", amazonCandidates, amazonService::search).join());
        prices.put("rakuten", runCandidates("RAKUTEN", rakutenCandidates, rakutenService::search).join());
        prices.put("naver", runCandidates("NAVER", naverCandidates, naverService::search).join());
        prices.put("coupang", runCandidates("COUPANG", coupangCandidates, coupangService::search).join());

        // =====================================================================
        // 🔥 STEP 3 — 환율
        // =====================================================================
        ExchangeRate rate = exchangeService.getRate();
        long jpyToKrw = rate.getJpyToKrw();
        double krwToJpy = rate.getKrwToJpy();


        // =====================================================================
        // 🔥 STEP 4 — 최저가 분석
        // =====================================================================
        PriceInfo best = prices.values().stream()
                .filter(pi -> pi != null && pi.getPriceKrw() > 0)
                .min(Comparator.comparingLong(PriceInfo::getPriceKrw))
                .orElse(null);

        String bestPlatform = (best != null) ? best.getPlatform() : "-";
        long minKrw = (best != null) ? best.getPriceKrw() : 0;
        long minJpy = (long) (minKrw * krwToJpy);

        // =====================================================================
        // 🔥 STEP 5 — 결과 생성
        // =====================================================================
        MarginCompareResult result = MarginCompareResult.builder()
                .keyword(keyword)
                .lang(lang)
                .platformPrices(prices)
                .bestPlatform(bestPlatform)
                .profitKrw(minKrw)
                .profitJpy(minJpy)
                .jpyToKrw((double) jpyToKrw)
                .aiAnalysis(null)
                .build();

        // =====================================================================
        // 🔥 STEP 6 — AI 분석
        // =====================================================================
        try {
            AiMarginAnalysis analysis = aiService.analyze(result);
            result.setAiAnalysis(analysis);
        } catch (Exception e) {
            log.error("❌ AI 분석 실패: {}", e.getMessage());
        }

        // =====================================================================
        // 🔥 STEP 7 — 출력 번역
        // =====================================================================
        applyOutputTranslation(result, lang, isEnglish);

        return result;
    }

    // =====================================================================
    // 🔥 후보 검색 실행
    // =====================================================================
    private CompletableFuture<PriceInfo> runCandidates(
            String platform,
            List<String> candidates,
            Function<String, PriceInfo> searchFn
    ) {
        return CompletableFuture.supplyAsync(() -> {
            for (String c : candidates) {
                try {
                    PriceInfo pi = searchFn.apply(c);
                    if (pi != null && pi.getPriceKrw() > 0) return pi;
                } catch (Exception ignore) {}
            }
            return error(platform);
        }, executor).completeOnTimeout(error(platform), TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    // =====================================================================
    // 🔥 번역 / 출력 규칙
    // =====================================================================
    private void applyOutputTranslation(MarginCompareResult r, String lang, boolean english) {
        if (english) {
            if (lang.equals("ko")) translateToKo(r);
            else translateToJp(r);
            return;
        }

        if (lang.equals("ko")) translateToKo(r);
        else translateToJp(r);
    }

    private void translateToKo(MarginCompareResult r) {
        r.getPlatformPrices().values().forEach(pi -> {
            if (pi != null && pi.getProductName() != null)
                pi.setProductName(translateService.jpToKo(pi.getProductName()));
        });
    }

    private void translateToJp(MarginCompareResult r) {
        r.getPlatformPrices().values().forEach(pi -> {
            if (pi != null && pi.getProductName() != null)
                pi.setProductName(translateService.koToJp(pi.getProductName()));
        });
    }

    // =====================================================================
    // 🔥 에러 PriceInfo
    // =====================================================================
    private PriceInfo error(String platform) {
        return PriceInfo.builder()
                .platform(platform)
                .productName("조회 실패")
                .productUrl("")
                .productImage("")
                .currencyOriginal("KRW")
                .priceKrw(0)
                .priceJpy(0)
                .build();
    }
}
