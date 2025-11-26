package com.ecprice_research.domain.margin.service;

import com.ecprice_research.domain.margin.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicMarginService {

    public PriceInfo pickBest(PriceInfo... arr) {
        PriceInfo best = null;

        for (PriceInfo p : arr) {
            if (p == null || p.getPriceKrw() == null) continue;

            if (best == null ||
                    p.getPriceKrw() < best.getPriceKrw()) {
                best = p;
            }
        }
        return best;
    }

    public AiMarginAnalysis buildAnalysis(PriceInfo best, MarginRequest req) {

        if (best == null) return null;

        return AiMarginAnalysis.builder()
                .buyPlatform(best.getPlatform())
                .sellPlatform(req.getLang().equals("kr") ? "NAVER" : "AMAZON_JP")
                .profitKrw(best.getPriceKrw())
                .profitRate(0.0)
                .text("Basic 분석: " + best.getPlatform() + "이 최저가입니다.")
                .reason("basic")
                .build();
    }
}
