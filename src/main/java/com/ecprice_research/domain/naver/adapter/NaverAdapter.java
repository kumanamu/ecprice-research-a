package com.ecprice_research.domain.naver.adapter;

import com.ecprice_research.domain.keyword.engine.KeywordAdapter.PlatformAdapter;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.naver.service.NaverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NaverAdapter implements PlatformAdapter {

    private final NaverService service;

    @Override
    public PriceInfo searchSingle(String keyword) {
        return service.searchSingle(keyword);
    }
}
