package com.ecprice_research.domain.search.adapter;

import com.ecprice_research.domain.rakuten.service.RakutenService;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Rakuten Japan 검색 어댑터
 */
@Component("RAKUTEN")
@RequiredArgsConstructor
public class RakutenSearchAdapter implements ShopAdapter {

    private final RakutenService rakutenService;

    @Override
    public String getPlatformName() {
        return "RAKUTEN";
    }

    @Override
    public PriceInfo search(String keyword) {
        return rakutenService.search(keyword);
    }
}
