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
import com.ecprice_research.util.KeywordVariantCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

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
    // 🔥 메인 비교 로직
    // =====================================================================
    public MarginCompareResult compare(String keyword, String lang) {

        log.info("🔍 Margin Compare 실행: keyword={}, lang={}", keyword, lang);

        // ───────────────────────────────
        // 🔥 Step 1)  영어 여부 체크
        // ───────────────────────────────
        boolean isEnglishOnly = keyword.matches("^[a-zA-Z0-9\\s\\-_.]+$");

        List<String> candidatesAmazon;
        List<String> candidatesRakuten;
        List<String> candidatesNaver;
        List<String> candidatesCoupang;

        // ───────────────────────────────
        // 🔥 Step 2) 검색 후보 생성
        // ───────────────────────────────
        if (isEnglishOnly) {

            // 영어 → 모든 플랫폼 "그대로 검색"
            candidatesAmazon = List.of(keyword);
            candidatesRakuten = List.of(keyword);
            candidatesNaver = List.of(keyword);
            candidatesCoupang = List.of(keyword);

        } else if (lang.equals("ko")) {

            // 한국어 토글

            // 아마존 / 라쿠텐 → 일본어로 번역
            String jp = translateService.koToJp(keyword);

            candidatesAmazon = KeywordVariantCache.buildCandidates(keyword, "ko", translateService);
            candidatesRakuten = List.of(jp);

            // 네이버 / 쿠팡 → 한국어 그대로
            candidatesNaver = List.of(keyword);
            candidatesCoupang = List.of(keyword);

        } else {

            // 일본어 토글

            // 네이버 / 쿠팡 → 한국어로 번역
            String ko = translateService.jpToKo(keyword);

            candidatesAmazon = KeywordVariantCache.bu(keyword, "jp", translateService);
            candidatesRakuten = List.of(keyword);
            candidatesNaver = List.of(ko);
            candidatesCoupang = List.of(ko);
        }

        // 로그 출력
        log.info("🔍 [Amazon] 검색 후보: {}", candidatesAmazon);
        log.info("🔍 [Rakuten] 검색 후보: {}", candidatesRakuten);
        log.info("🔍 [Naver] 검색 후보: {}", candidatesNaver);
        log.info("🔍 [Coupang] 검색 후보: {}", candidatesCoupang);


        // =====================================================================
        // 🔥 Step 3) 병렬 검색
        // =====================================================================
        CompletableFuture<PriceInfo> amazonFuture =
                runCandidates("AMAZON_JP", candidatesAmazon, amazonService::search);

        CompletableFuture<PriceInfo> rakutenFuture =
                runCandidates("RAKUTEN", candidatesRakuten, rakutenService::search);

        CompletableFuture<PriceInfo> naverFuture =
                runCandidates("NAVER", candidatesNaver, naverService::search);

        CompletableFuture<PriceInfo> coupangFuture =
                runCandidates("COUPANG", candidatesCoupang, coupangService::search);

        PriceInfo amazon = amazonFuture.join();
        PriceInfo rakuten = rakutenFuture.join();
        PriceInfo naver = naverFuture.join();
        PriceInfo coupang = coupangFuture.join();


        // =====================================================================
        // 🔥 Step 4) 환율 호출
        // =====================================================================
        ExchangeRate exchangeRate = exchangeService.getRate();
        double krwToJpy = exchangeRate.getKrwToJpy();
        long jpyToKrw = exchangeRate.getJpyToKrw();

        log.info("💱 실시간 환율: 1 JPY = {} KRW, 1 KRW = {} JPY", jpyToKrw, krwToJpy);


        // =====================================================================
        // 🔥 Step 5) 최저가 판단
        // =====================================================================
        String best = findBestPlatform(amazon, rakuten, naver, coupang);
        long lowest = findLowestPrice(amazon, rakuten, naver, coupang);


        // =====================================================================
        // 🔥 Step 6) 결과 생성
        // =====================================================================
        MarginCompareResult result = MarginCompareResult.builder()
                .keyword(keyword)
                .lang(lang)
                .amazonJp(amazon)
                .rakuten(rakuten)
                .naver(naver)
                .coupang(coupang)
                .bestPlatform(best)
                .profitKrw(lowest)
                .profitJpy((long) (lowest * krwToJpy))
                .krwToJpy(krwToJpy)
                .jpyToKrw(jpyToKrw)
                .build();


        // =====================================================================
        // 🔥 Step 7) AI 분석 (이미 안정화됨)
        // =====================================================================
        try {
            AiMarginAnalysis analysis = aiService.analyze(result);
            result.setAiAnalysis(analysis);

            if (analysis != null) {
                log.info("🤖 AI Margin 분석 요약: {}", analysis.summary());
            }
        } catch (Exception e) {
            log.error("❌ AI 분석 실패: {}", e.getMessage());
        }


        // =====================================================================
        // 🔥 Step 8) 출력 번역 (토글 규칙)
        // =====================================================================
        try {
            applyOutputTranslation(result, lang, isEnglishOnly);

        } catch (Exception e) {
            log.error("❌ 결과 번역 실패: {}", e.getMessage());
        }


        return result;
    }


    // =====================================================================
    // 🔥 플랫폼별 후보 리스트를 순회하며 처음 성공한 값 반환
    // =====================================================================
    private CompletableFuture<PriceInfo> runCandidates(
            String platform,
            List<String> candidates,
            java.util.function.Function<String, PriceInfo> searchFn
    ) {
        return CompletableFuture.supplyAsync(() -> {

            for (String k : candidates) {
                try {
                    PriceInfo pi = searchFn.apply(k);
                    if (pi != null && pi.getPriceKrw() > 0) return pi;
                } catch (Exception ignored) {}
            }

            return error(platform);

        }, executor).completeOnTimeout(error(platform), TIMEOUT_SEC, TimeUnit.SECONDS);
    }


    // =====================================================================
    // 🔥 출력 번역 규칙
    // =====================================================================
    private void applyOutputTranslation(MarginCompareResult r, String lang, boolean englishInput) {

        if (englishInput) {
            if (lang.equals("ko")) {
                translateToKo(r);
            } else if (lang.equals("jp")) {
                translateToJp(r);
            }
            return;
        }

        if (lang.equals("ko")) {
            translateToKo(r);
        } else if (lang.equals("jp")) {
            translateToJp(r);
        }
    }

    private void translateToKo(MarginCompareResult r) {
        r.getAmazonJp().setProductName(translateService.jpToKo(r.getAmazonJp().getProductName()));
        r.getRakuten().setProductName(translateService.jpToKo(r.getRakuten().getProductName()));
    }

    private void translateToJp(MarginCompareResult r) {
        r.getNaver().setProductName(translateService.koToJp(r.getNaver().getProductName()));
        r.getCoupang().setProductName(translateService.koToJp(r.getCoupang().getProductName()));
    }


    // =====================================================================
    // 🔥 최저가 계산
    // =====================================================================
    private long p(PriceInfo x) {
        return (x != null && x.getPriceKrw() > 0) ? x.getPriceKrw() : Long.MAX_VALUE;
    }

    private String findBestPlatform(PriceInfo a, PriceInfo r, PriceInfo n, PriceInfo c) {
        long aa = p(a), rr = p(r), nn = p(n), cc = p(c);
        long min = Math.min(Math.min(aa, rr), Math.min(nn, cc));

        if (min == aa) return "AMAZON_JP";
        if (min == rr) return "RAKUTEN";
        if (min == nn) return "NAVER";
        return "COUPANG";
    }

    private long findLowestPrice(PriceInfo a, PriceInfo r, PriceInfo n, PriceInfo c) {
        return Math.min(Math.min(p(a), p(r)), Math.min(p(n), p(c)));
    }


    // =====================================================================
    // 🔥 공통 에러 응답
    // =====================================================================
    private PriceInfo error(String platform) {
        return PriceInfo.builder()
                .platform(platform)
                .productName("조회 실패")
                .productUrl("")
                .productImage("")
                .priceKrw(0)
                .currencyOriginal("KRW")
                .build();
    }
}
