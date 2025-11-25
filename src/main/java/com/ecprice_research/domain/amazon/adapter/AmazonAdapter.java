package com.ecprice_research.domain.amazon.adapter;

import com.ecprice_research.domain.amazon.service.AmazonService;
import com.ecprice_research.domain.keyword.engine.KeywordAdapter.PlatformAdapter;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AmazonAdapter implements PlatformAdapter {

    private final AmazonService service;

    @Override
    public PriceInfo searchSingle(String keyword) {
        return service.searchSingle(keyword);
    }
}
