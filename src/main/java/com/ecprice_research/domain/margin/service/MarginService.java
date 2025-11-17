package com.ecprice_research.domain.margin.service;

import com.ecprice_research.domain.amazon.service.AmazonService;
import com.ecprice_research.domain.coupang.service.CoupangService;
import com.ecprice_research.domain.margin.dto.AiMarginAnalysis;
import com.ecprice_research.domain.margin.dto.MarginCompareResult;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.naver.service.NaverService;
import com.ecprice_research.domain.rakuten.service.RakutenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarginService {

    private final AmazonService amazonService;
    private final RakutenService rakutenService;
    private final NaverService naverService;
    private final CoupangService coupangService;
    private final OpenAiAnalysisService aiService;

    public MarginCompareResult compare(String keyword, String lang) {

        // 각 서비스에서 List<PriceInfo>를 반환받고 첫 번째 상품만 추출
        CompletableFuture<PriceInfo> amazonFuture =
                CompletableFuture.supplyAsync(() -> {
                    List<PriceInfo> list = amazonService.search(keyword);
                    return (list != null && !list.isEmpty()) ? list.get(0) : createErrorPrice("AMAZON_JP");
                });

        CompletableFuture<PriceInfo> rakutenFuture =
                CompletableFuture.supplyAsync(() -> {
                    List<PriceInfo> list = rakutenService.search(keyword);
                    return (list != null && !list.isEmpty()) ? list.get(0) : createErrorPrice("RAKUTEN");
                });

        CompletableFuture<PriceInfo> naverFuture =
                CompletableFuture.supplyAsync(() -> {
                    List<PriceInfo> list = naverService.search(keyword);
                    return (list != null && !list.isEmpty()) ? list.get(0) : createErrorPrice("NAVER");
                });

        CompletableFuture<PriceInfo> coupangFuture =
                CompletableFuture.supplyAsync(() -> {
                    List<PriceInfo> list = coupangService.search(keyword);
                    return (list != null && !list.isEmpty()) ? list.get(0) : createErrorPrice("COUPANG");
                });

        // 모든 비동기 작업 완료 대기
        PriceInfo amazon = amazonFuture.join();
        PriceInfo rakuten = rakutenFuture.join();
        PriceInfo naver = naverFuture.join();
        PriceInfo coupang = coupangFuture.join();

        // 환율 (더미 값)
        double krwToJpy = 0.1;
        long jpyToKrw = 10;

        // 최저가 플랫폼 찾기
        String bestPlatform = findBestPlatform(amazon, rakuten, naver, coupang);
        long profitKrw = findLowestPrice(amazon, rakuten, naver, coupang);

        MarginCompareResult result = MarginCompareResult.builder()
                .keyword(keyword)
                .lang(lang)
                .amazonJp(amazon)
                .rakuten(rakuten)
                .naver(naver)
                .coupang(coupang)
                .jpyToKrw(jpyToKrw)
                .krwToJpy(krwToJpy)
                .bestPlatform(bestPlatform)
                .profitKrw(profitKrw)
                .profitJpy((long)(profitKrw * krwToJpy))
                .build();

        // 🔥 AI 분석 추가
        AiMarginAnalysis analysis = aiService.analyze(result);
        result.setAiAnalysis(analysis);

        return result;
    }

    /**
     * 최저가 플랫폼 찾기
     */
    private String findBestPlatform(PriceInfo amazon, PriceInfo rakuten, PriceInfo naver, PriceInfo coupang) {
        long amazonPrice = amazon.getPriceKrw() > 0 ? amazon.getPriceKrw() : Long.MAX_VALUE;
        long rakutenPrice = rakuten.getPriceKrw() > 0 ? rakuten.getPriceKrw() : Long.MAX_VALUE;
        long naverPrice = naver.getPriceKrw() > 0 ? naver.getPriceKrw() : Long.MAX_VALUE;
        long coupangPrice = coupang.getPriceKrw() > 0 ? coupang.getPriceKrw() : Long.MAX_VALUE;

        long min = Math.min(Math.min(amazonPrice, rakutenPrice), Math.min(naverPrice, coupangPrice));

        if (min == amazonPrice) return "AMAZON_JP";
        if (min == rakutenPrice) return "RAKUTEN";
        if (min == naverPrice) return "NAVER";
        if (min == coupangPrice) return "COUPANG";
        return "NAVER";
    }

    /**
     * 최저가 구하기 (KRW 기준)
     */
    private long findLowestPrice(PriceInfo amazon, PriceInfo rakuten, PriceInfo naver, PriceInfo coupang) {
        long amazonPrice = amazon.getPriceKrw() > 0 ? amazon.getPriceKrw() : Long.MAX_VALUE;
        long rakutenPrice = rakuten.getPriceKrw() > 0 ? rakuten.getPriceKrw() : Long.MAX_VALUE;
        long naverPrice = naver.getPriceKrw() > 0 ? naver.getPriceKrw() : Long.MAX_VALUE;
        long coupangPrice = coupang.getPriceKrw() > 0 ? coupang.getPriceKrw() : Long.MAX_VALUE;

        return Math.min(Math.min(amazonPrice, rakutenPrice), Math.min(naverPrice, coupangPrice));
    }

    /**
     * 에러 PriceInfo 생성
     */
    private PriceInfo createErrorPrice(String platform) {
        return PriceInfo.builder()
                .platform(platform)
                .productName("조회 실패")
                .currencyOriginal("KRW")
                .priceKrw(0)
                .build();
    }
}