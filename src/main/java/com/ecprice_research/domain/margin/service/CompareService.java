package com.ecprice_research.domain.margin.service;

import com.ecprice_research.domain.margin.dto.*;
import com.ecprice_research.domain.amazon.service.AmazonService;
import com.ecprice_research.domain.coupang.service.CoupangService;
import com.ecprice_research.domain.rakuten.service.RakutenService;
import com.ecprice_research.domain.naver.service.NaverService;
import com.ecprice_research.domain.exchange.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompareService {

    private final AmazonService amazon;
    private final RakutenService rakuten;
    private final CoupangService coupang;
    private final NaverService naver;
    private final ExchangeService exchange;

    private PlatformPrice toPlatform(PriceInfo p) {
        if (p == null) return new PlatformPrice();

        return PlatformPrice.builder()
                .productName(p.getProductName())
                .productUrl(p.getProductUrl())
                .productImage(p.getProductImage())
                .priceKrw(p.getPriceKrw())
                .priceJpy(p.getPriceJpy())
                .build();
    }

    public CompareResultDto compare(String keyword) {

        var a = amazon.search(keyword);
        var r = rakuten.search(keyword);
        var n = naver.search(keyword);
        var c = coupang.search(keyword);

        double jpyToKrw = exchange.getRate().getJpyToKrw();

        // 최저가 계산
        double min = Double.MAX_VALUE;
        String best = "-";

        if (a != null && a.getPriceKrw() > 0 && a.getPriceKrw() < min) { min = a.getPriceKrw(); best = "AmazonJP"; }
        if (r != null && r.getPriceKrw() > 0 && r.getPriceKrw() < min) { min = r.getPriceKrw(); best = "Rakuten"; }
        if (n != null && n.getPriceKrw() > 0 && n.getPriceKrw() < min) { min = n.getPriceKrw(); best = "Naver"; }
        if (c != null && c.getPriceKrw() > 0 && c.getPriceKrw() < min) { min = c.getPriceKrw(); best = "Coupang"; }

        return CompareResultDto.builder()
                .amazonJp(toPlatform(a))
                .rakuten(toPlatform(r))
                .naver(toPlatform(n))
                .coupang(toPlatform(c))
                .bestPlatform(best)
                .jpyToKrw(jpyToKrw)
                .build();
    }
}
