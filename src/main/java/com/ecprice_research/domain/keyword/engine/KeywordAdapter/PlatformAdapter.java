package com.ecprice_research.domain.keyword.engine.KeywordAdapter;

import com.ecprice_research.domain.margin.dto.PriceInfo;

public interface PlatformAdapter {
    PriceInfo searchSingle(String keyword);
}
