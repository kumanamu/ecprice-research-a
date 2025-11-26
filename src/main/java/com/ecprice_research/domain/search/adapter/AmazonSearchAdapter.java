package com.ecprice_research.domain.search.adapter;

import com.ecprice_research.domain.amazon.service.AmazonService;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Amazon Japan 검색 어댑터
 */
@Component("AMAZON_JP")
@RequiredArgsConstructor
public class AmazonSearchAdapter implements ShopAdapter {

    private final AmazonService amazonService;

    @Override
    public String getPlatformName() {
        return "AMAZON_JP";
    }

    @Override
    public PriceInfo search(String keyword) {
        return amazonService.search(keyword);
    }
}
