package com.ecprice_research.domain.search.engine;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.search.adapter.ShopAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * C-ENGINE의 핵심: Adapter들을 호출하여 PriceInfo 리스트를 만들어내는 엔진
 * - variants(후보키워드)
 * - adapters(쇼핑몰 4개)
 * 를 조합해서 모든 검색결과를 수집한다.
 */
@Component
@RequiredArgsConstructor
public class SearchDispatcher {

    private final List<ShopAdapter> adapters;

    /**
     * variants × adapters 순회하면서 모든 PriceInfo 수집
     */
    public List<PriceInfo> dispatch(List<String> variants) {
        List<PriceInfo> result = new ArrayList<>();

        for (String keyword : variants) {
            for (ShopAdapter adapter : adapters) {
                try {
                    PriceInfo info = adapter.search(keyword);
                    result.add(info);
                } catch (Exception e) {
                    // 안전성을 위해 NOT_FOUND 처리
                    result.add(
                            PriceInfo.notFound(
                                    adapter.getPlatformName(),
                                    "Dispatcher Error: " + e.getMessage()
                            )
                    );
                }
            }
        }

        return result;
    }
}
