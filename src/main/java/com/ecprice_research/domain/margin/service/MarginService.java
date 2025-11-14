package com.ecprice_research.domain.margin.service;

import com.ecprice_research.domain.amazon.service.AmazonService;
import com.ecprice_research.domain.coupang.service.CoupangService;
import com.ecprice_research.domain.exchange.dto.ExchangeRate;
import com.ecprice_research.domain.exchange.service.ExchangeService;
import com.ecprice_research.domain.margin.dto.MarginCompareResult;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.naver.service.NaverService;
import com.ecprice_research.domain.rakuten.service.RakutenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarginService {

    private final AmazonService amazonService;
    private final RakutenService rakutenService;
    private final NaverService naverService;
    private final CoupangService coupangService;
    private final ExchangeService exchangeService;

    /**
     * 가격 전체 비교 → 최저가 / 최고마진 플랫폼 선정
     */
    public MarginCompareResult compareAll(String keyword) {

        PriceInfo amazon = amazonService.search(keyword);
        PriceInfo rakuten = rakutenService.search(keyword);
        PriceInfo naver = naverService.search(keyword);
        PriceInfo coupang = coupangService.search(keyword);

        ExchangeRate rate = exchangeService.getRate();

        long jpyToKrw = rate.getJpyToKrw();
        double krwToJpy = rate.getKrwToJpy();

        // 가격 KRW 기준 정규화
        convertPrices(amazon, jpyToKrw);
        convertPrices(rakuten, jpyToKrw);
        convertPrices(naver, 1);
        convertPrices(coupang, 1);

        // 최고가 = 가장 비싼 = 가장 팔면 이익 많이남
        PriceInfo best = amazon;
        long profit = amazon.getPriceKrw();

        if (rakuten.getPriceKrw() > profit) { best = rakuten; profit = rakuten.getPriceKrw(); }
        if (naver.getPriceKrw() > profit)    { best = naver; profit = naver.getPriceKrw(); }
        if (coupang.getPriceKrw() > profit)  { best = coupang; profit = coupang.getPriceKrw(); }

        return MarginCompareResult.builder()
                .keyword(keyword)
                .amazonJp(amazon)
                .rakuten(rakuten)
                .naver(naver)
                .coupang(coupang)
                .jpyToKrw(jpyToKrw)
                .krwToJpy(krwToJpy)
                .bestPlatform(best.getPlatform())
                .profitKrw(profit)
                .profitJpy((long) (profit * krwToJpy))
                .build();
    }

    private void convertPrices(PriceInfo p, long jpyToKrw) {
        if (p.getCurrencyOriginal().equals("JPY")) {
            p.setPriceKrw(p.getPriceOriginal() * jpyToKrw);
            p.setPriceJpy(p.getPriceOriginal());
        } else {
            p.setPriceKrw(p.getPriceOriginal());
            p.setPriceJpy(0);
        }
    }
}
