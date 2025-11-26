package com.ecprice_research.domain.search.engine;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import java.util.Comparator;
import java.util.List;

public class SearchAggregator {

    /**
     * 여러 PriceInfo 리스트 중 성공한 결과만 필터링 후 최저가 선택
     */
    public PriceInfo pickLowest(List<PriceInfo> list) {

        return list.stream()
                .filter(info -> info != null && info.isSuccess())
                .min(Comparator.comparing(PriceInfo::getPriceKrw))
                .orElse(PriceInfo.notFound("UNKNOWN", "no valid item"));
    }
}