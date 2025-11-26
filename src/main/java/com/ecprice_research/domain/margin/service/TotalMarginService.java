package com.ecprice_research.domain.margin.service;

import com.ecprice_research.domain.amazon.service.AmazonService;
import com.ecprice_research.domain.coupang.service.CoupangService;
import com.ecprice_research.domain.exchange.service.ExchangeService;
import com.ecprice_research.domain.margin.dto.*;
import com.ecprice_research.domain.naver.service.NaverService;
import com.ecprice_research.domain.openai.OpenAiClient;
import com.ecprice_research.domain.rakuten.service.RakutenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TotalMarginService {

    private final AmazonService amazon;
    private final RakutenService rakuten;
    private final NaverService naver;
    private final CoupangService coupang;
    private final ExchangeService exchangeService;

    private final OpenAiClient ai;

    public MarginResponse compare(MarginRequest req) {

        // 🔥 실시간 환율
        var rate = exchangeService.getRate();
        double jpyToKrw = rate.getJpyToKrw();
        double krwToJpy = rate.getKrwToJpy();

        // 🔥 각 플랫폼 조회
        PriceInfo a = amazon.search(req.getKeyword());
        PriceInfo r = rakuten.search(req.getKeyword());
        PriceInfo n = naver.search(req.getKeyword());
        PriceInfo c = coupang.search(req.getKeyword());

        // 🔥 JPY → KRW 변환
        convertCurrency(a, jpyToKrw);
        convertCurrency(r, jpyToKrw);
        convertCurrency(n, jpyToKrw); // naver는 KRW이므로 영향 X
        convertCurrency(c, jpyToKrw);

        // 플랫폼 맵 (AI 분석에 활용)
        Map<String, PriceInfo> map = new LinkedHashMap<>();
        map.put("AMAZON_JP", a);
        map.put("RAKUTEN", r);
        map.put("NAVER", n);
        map.put("COUPANG", c);

        // 🔥 최저가 선정
        PriceInfo best = pickBest(a, r, n, c);

        // 🔥 Basic AI / Premium AI 생성
        AiMarginAnalysis basicAi = buildBasicAnalysis(map, best);
        AiMarginAnalysis premiumAi = buildPremiumAnalysis(map, best);

        // 🔥 최종 Response
        return MarginResponse.builder()
                .keyword(req.getKeyword())
                .lang(req.getLang())
                .jpyToKrw(jpyToKrw)
                .krwToJpy(krwToJpy)
                .amazon(a)
                .rakuten(r)
                .naver(n)
                .coupang(c)
                .best(best)
                .basicAi(basicAi)
                .premiumAi(premiumAi)
                .build();
    }

    // ---------------------------
    // 가격 변환
    // ---------------------------
    private void convertCurrency(PriceInfo p, double jpyToKrw) {
        if (p == null || !"SUCCESS".equals(p.getStatus())) return;

        if ("JPY".equalsIgnoreCase(p.getCurrencyOriginal())) {
            p.setPriceJpy(p.getPriceOriginal());
            p.setPriceKrw((int) Math.round(p.getPriceOriginal() * jpyToKrw));
        } else {
            p.setPriceKrw(p.getPriceOriginal());
            p.setPriceJpy((int) Math.round(p.getPriceOriginal() / jpyToKrw));
        }
    }

    // ---------------------------
    // 최저가
    // ---------------------------
    private PriceInfo pickBest(PriceInfo... items) {
        PriceInfo best = null;

        for (PriceInfo p : items) {
            if (p == null || !"SUCCESS".equals(p.getStatus())) continue;
            if (p.getPriceKrw() == null) continue;

            if (best == null || p.getPriceKrw() < best.getPriceKrw()) {
                best = p;
            }
        }

        if (best == null)
            return PriceInfo.notFound("NONE", "NO_VALID_PRICE");

        return best;
    }

    // ---------------------------
    // BASIC AI
    // ---------------------------
    private AiMarginAnalysis buildBasicAnalysis(Map<String, PriceInfo> map, PriceInfo best) {

        String prompt = """
                🔍 Basic Summary (KRW 기준)

                플랫폼별 가격:
                %s

                최저가 플랫폼: %s
                최저가 금액: %d KRW

                핵심 요약만 5줄 이내로.
                """.formatted(
                priceLines(map),
                best.getPlatform(),
                best.getPriceKrw()
        );

        String answer = ai.ask(prompt);

        return AiMarginAnalysis.builder()
                .text(answer)
                .reason("basic-ai")
                .build();
    }

    // ---------------------------
    // PREMIUM AI
    // ---------------------------
    private AiMarginAnalysis buildPremiumAnalysis(Map<String, PriceInfo> map, PriceInfo best) {

        String prompt = """
                🔥 Premium Market Insight
                (한국어 + 일본어 모두)

                플랫폼별 가격(KRW/JPY):
                %s

                최저가 플랫폼: %s (%d KRW)

                1) 시장성 분석
                2) 가격 경쟁력
                3) 리셀 전략
                """.formatted(
                priceLines(map),
                best.getPlatform(),
                best.getPriceKrw()
        );

        String answer = ai.ask(prompt);

        return AiMarginAnalysis.builder()
                .text(answer)
                .reason("premium-ai")
                .build();
    }

    private String priceLines(Map<String, PriceInfo> map) {
        StringBuilder sb = new StringBuilder();
        map.forEach((k, v) -> {
            sb.append(k).append(": ");

            if (!"SUCCESS".equals(v.getStatus())) {
                sb.append("NOT_FOUND\n");
                return;
            }

            sb.append(v.getPriceKrw()).append(" KRW");
            sb.append(" (").append(v.getPriceJpy()).append(" JPY)");
            sb.append("\n");
        });
        return sb.toString();
    }
}
