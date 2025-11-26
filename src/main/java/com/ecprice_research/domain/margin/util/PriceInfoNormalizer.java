package com.ecprice_research.domain.margin.util;

import com.ecprice_research.domain.margin.dto.MarginRequest;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import org.springframework.stereotype.Component;

@Component
public class PriceInfoNormalizer {

    public void normalize(PriceInfo p, MarginRequest req) {
        if (p == null) return;
        if (p.getPriceOriginal() == null) return;

        // KRW 변환
        if ("JPY".equalsIgnoreCase(p.getCurrencyOriginal())) {
            int krw = (int) Math.round(p.getPriceOriginal() * req.getKrwRate());
            p.setPriceKrw(krw);
            p.setPriceJpy(p.getPriceOriginal());
            p.setDisplayPrice(String.format("%,d KRW (%,d JPY)", krw, p.getPriceOriginal()));
        }

        // KRW 기반 (네이버/쿠팡)
        else {
            int jpy = (int) Math.round(p.getPriceOriginal() * req.getJpyRate());
            p.setPriceKrw(p.getPriceOriginal());
            p.setPriceJpy(jpy);
            p.setDisplayPrice(String.format("%,d KRW (%,d JPY)", p.getPriceOriginal(), jpy));
        }
    }
}
