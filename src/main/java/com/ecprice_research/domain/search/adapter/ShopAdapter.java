package com.ecprice_research.domain.search.adapter;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import java.util.List;

/**
 * 모든 쇼핑몰 검색 Adapter의 공통 인터페이스 (EC-ENGINE의 핵심 규약)
 * Amazon / Rakuten / Naver / Coupang Search Adapter의 표준
 */
public interface ShopAdapter {

    /**
     * 플랫폼명 (AMAZON_JP / RAKUTEN / NAVER_SHOPPING / COUPANG)
     */
    String getPlatformName();

    /**
     * 단일 키워드 검색
     * 각 Adapter는 사이트별 API 문서 규칙 그대로 구현함
     */
    PriceInfo search(String keyword);

    /**
     * 여러 후보키워드를 Premium 모드에서 순차 검색
     */
    default List<PriceInfo> searchVariants(List<String> variants) {
        return variants.stream()
                .map(this::search)
                .toList();
    }
}
