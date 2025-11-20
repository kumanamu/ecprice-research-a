package com.ecprice_research.domain.margin.service;

import com.ecprice_research.domain.margin.dto.MarginSimulationRequest;
import com.ecprice_research.domain.margin.dto.MarginSimulationResult;
import com.ecprice_research.domain.margin.dto.CompareResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutoMarginService {

    private final MarginSimulationService sim;

    public CompareResultDto attachAutoMargin(CompareResultDto raw) {

        // 최저가 KRW 찾기
        double purchase = Math.min(
                Math.min(raw.getAmazonJp().getPriceKrw(), raw.getRakuten().getPriceKrw()),
                Math.min(raw.getNaver().getPriceKrw(), raw.getCoupang().getPriceKrw())
        );

        MarginSimulationRequest req = new MarginSimulationRequest();
        req.setPurchasePriceKrw((long) purchase);             // 🔥 캐스팅
        req.setSellPriceKrw((long) (purchase * 1.3));         // 🔥 캐스팅
        req.setExchangeRateJpyToKrw(raw.getJpyToKrw());

        var result = sim.simulate(req);

        raw.setProfitKrw(result.getProfitKrw());
        raw.setProfitJpy(result.getProfitJpy());

        return raw;
    }
}
