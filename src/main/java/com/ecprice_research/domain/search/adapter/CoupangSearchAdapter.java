package com.ecprice_research.domain.search.adapter;

import com.ecprice_research.domain.coupang.service.CoupangService;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Coupang API 검색 어댑터
 */
@Component("COUPANG")
@RequiredArgsConstructor
public class CoupangSearchAdapter implements ShopAdapter {

    private final CoupangService coupangService;

    @Override
    public String getPlatformName() {
        return "COUPANG";
    }

    @Override
    public PriceInfo search(String keyword) {
        return coupangService.search(keyword);
    }
}
