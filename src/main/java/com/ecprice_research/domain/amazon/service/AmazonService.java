package com.ecprice_research.domain.amazon.service;

import com.ecprice_research.config.AmazonConfig;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.util.KeywordVariantCache;
import com.ecprice_research.domain.translate.service.TranslateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonService {

    private final AmazonConfig config;
    private final TranslateService translateService;

    private final RestTemplate restTemplate = new RestTemplate();

    public PriceInfo search(String keyword) {

        try {
            // 1) 후보 키워드 생성
            String[] variants = buildAmazonVariants(keyword);

            // 2) 후보어 순차 검색 (캐시효과 + SerpAPI 안정성)
            for (String k : variants) {

                if (k == null || k.isBlank()) continue;

                String url = config.buildSearchUrl(k);
                log.info("📡 [Amazon 요청] {}", url);

                Map<String, Object> res = restTemplate.getForObject(url, Map.class);
                if (res == null) continue;

                List<Map<String, Object>> products =
                        (List<Map<String, Object>>) res.get("product_results");

                PriceInfo p1 = extract(products);
                if (p1 != null) return p1;

                List<Map<String, Object>> organic =
                        (List<Map<String, Object>>) res.get("organic_results");

                PriceInfo p2 = extract(organic);
                if (p2 != null) return p2;
            }

            return error();

        } catch (Exception e) {
            log.error("❌ Amazon Error: {}", e.getMessage());
            return error();
        }
    }


    // ---------------------------------------------------------------------
    // 후보 검색어 생성 (영어는 그대로 1개, 나머지는 번역 + 의미확장)
    // ---------------------------------------------------------------------
    private String[] buildAmazonVariants(String keyword) {

        // 캐시 HIT 확인
        String[] cached = KeywordVariantCache.get("AMZ_" + keyword);
        if (cached != null) {
            log.info("🔁 [Amazon 후보 캐시 HIT] {}", Arrays.toString(cached));
            return cached;
        }

        List<String> list = new ArrayList<>();

        boolean isEnglish = keyword.matches("^[a-zA-Z0-9\\s]+$");

        if (isEnglish) {
            list.add(keyword);
        } else {
            String jp = translateService.koToJp(keyword);
            String en = translateService.jpToKo(jp); // 영어 후보 생성은 필요 없으면 제거해도 됨
            list.add(jp);
            if (!jp.equals(keyword)) list.add(keyword);
        }

        String[] result = list.toArray(new String[0]);
        KeywordVariantCache.put("AMZ_" + keyword, result);

        log.info("🔍 [Amazon] 검색 후보: {}", Arrays.toString(result));
        return result;
    }


    private PriceInfo extract(List<Map<String, Object>> list) {
        if (list == null) return null;

        for (Map<String, Object> item : list) {

            long price = extractPrice(item);
            if (price <= 0) continue;

            return PriceInfo.builder()
                    .platform("AMAZON_JP")
                    .productName((String) item.getOrDefault("title", "Unknown"))
                    .productUrl((String) item.getOrDefault("link", ""))
                    .productImage((String) item.getOrDefault("thumbnail", ""))
                    .priceJpy(price)
                    .currencyOriginal("JPY")
                    .build();
        }
        return null;
    }


    private long extractPrice(Object obj) {

        if (obj instanceof Map<?, ?> map) {
            Object x = map.get("extracted_price");
            if (x != null) return parse(x);

            x = map.get("price");
            if (x != null) return parse(x);

            x = map.get("price_string");
            if (x != null) return parse(x);

            Object pr = map.get("price_range");
            if (pr instanceof Map<?, ?> r) {
                Object min = r.get("min_price");
                if (min != null) return parse(min);
            }

            Object prices = map.get("prices");
            if (prices instanceof List<?> list && !list.isEmpty()) {
                return parse(list.get(0));
            }
        }

        return 0;
    }

    private long parse(Object v) {
        try {
            if (v instanceof Number n) return n.longValue();
            if (v instanceof String s) {
                String num = s.replaceAll("[^0-9]", "");
                if (!num.isBlank()) return Long.parseLong(num);
            }
            if (v instanceof Map<?, ?> m) return parse(m.get("value"));
        } catch (Exception ignore) {}
        return 0;
    }

    private PriceInfo error() {
        return PriceInfo.builder()
                .platform("AMAZON_JP")
                .productName("조회 실패")
                .productUrl("")
                .productImage("")
                .priceJpy(0)
                .currencyOriginal("JPY")
                .build();
    }
}
