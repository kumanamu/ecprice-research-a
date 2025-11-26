package com.ecprice_research.domain.search.engine;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.search.adapter.ShopAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CEngine {

    private final Map<String, ShopAdapter> adapters;
    // 예: "AMAZON_JP" -> AmazonAdapter, "RAKUTEN" -> RakutenAdapter ...

    /**
     * 🔥 여러 후보 키워드 검색
     */
    public List<PriceInfo> run(List<String> variants) {
        return adapters.values().stream()
                .flatMap(a -> a.searchVariants(variants).stream())
                .toList();
    }

    /**
     * 🔥 단일 플랫폼 검색 지원
     */
    public PriceInfo runSingle(String platform, String keyword) {
        ShopAdapter adapter = adapters.get(platform);
        if (adapter == null) {
            return PriceInfo.notFound(platform, "Adapter not found");
        }
        return adapter.search(keyword);
    }
}
