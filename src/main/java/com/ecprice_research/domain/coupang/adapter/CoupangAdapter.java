package com.ecprice_research.domain.coupang.adapter;

import com.ecprice_research.domain.keyword.engine.KeywordAdapter.PlatformAdapter;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.coupang.service.CoupangService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoupangAdapter implements PlatformAdapter {

    private final CoupangService service;

    @Override
    public PriceInfo searchSingle(String keyword) {
        return service.searchSingle(keyword);
    }
}
