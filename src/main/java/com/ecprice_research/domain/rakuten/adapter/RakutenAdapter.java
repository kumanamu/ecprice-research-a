package com.ecprice_research.domain.rakuten.adapter;

import com.ecprice_research.domain.keyword.engine.KeywordAdapter.PlatformAdapter;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.rakuten.service.RakutenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RakutenAdapter implements PlatformAdapter {

    private final RakutenService service;

    @Override
    public PriceInfo searchSingle(String keyword) {
        return service.searchSingle(keyword);
    }
}
