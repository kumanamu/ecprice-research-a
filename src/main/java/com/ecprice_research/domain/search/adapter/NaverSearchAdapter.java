package com.ecprice_research.domain.search.adapter;

import com.ecprice_research.domain.naver.service.NaverService;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Naver Shopping 검색 어댑터
 */
@Component("NAVER_SHOPPING")
@RequiredArgsConstructor
public class NaverSearchAdapter implements ShopAdapter {

    private final NaverService naverService;

    @Override
    public String getPlatformName() {
        return "NAVER_SHOPPING";
    }

    @Override
    public PriceInfo search(String keyword) {
        return naverService.search(keyword);
    }
}
