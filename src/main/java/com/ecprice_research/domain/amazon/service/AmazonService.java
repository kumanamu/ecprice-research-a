package com.ecprice_research.domain.amazon.service;

import com.ecprice_research.config.AmazonConfig;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.util.KeywordVariantCache;
import com.ecprice_research.domain.translate.service.TranslateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
            List<String> candidates = buildVariants(keyword);
            log.info("🔍 [Amazon 후보] {}", candidates);

            for (String k : candidates) {
                if (k == null || k.isBlank()) continue;

                // 1) API 우선 검색
                PriceInfo apiResult = searchApi(k);
                if (apiResult != null && apiResult.getPriceJpy() > 0) {
                    return apiResult;
                }

                // 2) API 실패 → HTML fallback
                PriceInfo htmlResult = searchHtml(k);
                if (htmlResult != null && htmlResult.getPriceJpy() > 0) {
                    return htmlResult;
                }
            }

            return error();

        } catch (Exception e) {
            log.error("❌ Amazon Error: {}", e.getMessage());
            return error();
        }
    }

    // -----------------------------------------------------------
    // 🔥 1) SerpAPI (API) 검색
    // -----------------------------------------------------------
    private PriceInfo searchApi(String keyword) {
        try {
            String url = config.buildSearchUrl(keyword);
            log.info("📡 [Amazon API 요청] {}", url);

            Map<String, Object> res = restTemplate.getForObject(url, Map.class);
            if (res == null) return null;

            List<Map<String, Object>> organic = (List<Map<String, Object>>) res.get("organic_results");
            if (organic != null) {
                PriceInfo p = extractFromList(organic);
                if (p != null) return p;
            }

            List<Map<String, Object>> shopping = (List<Map<String, Object>>) res.get("shopping_results");
            if (shopping != null) {
                PriceInfo p = extractFromList(shopping);
                if (p != null) return p;
            }

        } catch (Exception e) {
            log.warn("⚠ Amazon API 실패: {}", e.getMessage());
        }
        return null;
    }

    private PriceInfo extractFromList(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) return null;

        for (Map<String, Object> item : list) {

            long price = extractPrice(item);
            if (price <= 0) continue;

            log.info("✅ [Amazon API 상품 발견] {} - {} JPY",
                    item.getOrDefault("title", "unknown"), price);

            return PriceInfo.builder()
                    .platform("AMAZON_JP")
                    .productName(String.valueOf(item.getOrDefault("title", "")))
                    .productUrl(String.valueOf(item.getOrDefault("link", "")))
                    .productImage(String.valueOf(item.getOrDefault("thumbnail", "")))
                    .priceJpy(price)
                    .currencyOriginal("JPY")
                    .build();
        }

        return null;
    }

    private long extractPrice(Object itemObj) {
        try {
            if (itemObj instanceof Map<?, ?> item) {

                Object v1 = item.get("extracted_price");
                if (v1 instanceof Number) return ((Number) v1).longValue();

                Object v2 = item.get("price");
                if (v2 instanceof Number) return ((Number) v2).longValue();

                Object v3 = item.get("price_string");
                if (v3 instanceof String s) {
                    String num = s.replaceAll("[^0-9]", "");
                    if (!num.isBlank()) return Long.parseLong(num);
                }
            }
        } catch (Exception ignored) {}
        return 0;
    }

    // -----------------------------------------------------------
    // 🔥 2) HTML fallback 검색 (정확도 최상)
    // -----------------------------------------------------------
    private PriceInfo searchHtml(String keyword) {
        try {
            String url = "https://www.amazon.co.jp/s?k=" + keyword;
            log.info("🌐 [Amazon HTML 요청] {}", url);

            Document doc = Jsoup.connect(url)
                    .timeout(8000)
                    .userAgent("Mozilla/5.0")
                    .get();

            // Amazon 검색 결과 슬롯
            for (Element item : doc.select(".s-main-slot .s-result-item")) {

                // 가격
                String priceStr = item.select(".a-price .a-offscreen").text();
                if (priceStr == null || priceStr.isBlank()) continue;

                String num = priceStr.replaceAll("[^0-9]", "");
                if (num.isBlank()) continue;

                long price = Long.parseLong(num);

                // 상품명
                String title = item.select("h2 a.a-link-normal").text();
                if (title.isBlank()) continue;

                // URL
                String link = "https://amazon.co.jp" + item.select("h2 a").attr("href");

                // 이미지
                String img = item.select("img.s-image").attr("src");

                log.info("🟢 [Amazon HTML 상품 발견] {} - {} JPY", title, price);

                return PriceInfo.builder()
                        .platform("AMAZON_JP")
                        .productName(title)
                        .productUrl(link)
                        .productImage(img)
                        .priceJpy(price)
                        .currencyOriginal("JPY")
                        .build();
            }

        } catch (Exception e) {
            log.warn("⚠ Amazon HTML 파싱 실패: {}", e.getMessage());
        }

        return null;
    }

    // -----------------------------------------------------------
    // 🔥 검색 후보 생성 (지침 유지)
    // -----------------------------------------------------------
    private List<String> buildVariants(String keyword) {

        List<String> cached = KeywordVariantCache.get("AMZ_" + keyword);
        if (cached != null) return cached;

        List<String> list = new ArrayList<>();

        boolean isEng = keyword.matches("^[a-zA-Z0-9\\s]+$");
        boolean isKor = keyword.matches(".*[가-힣].*");
        boolean isJap = keyword.matches(".*[ぁ-んァ-ン一-龥].*");

        if (isEng) list.add(keyword);
        else if (isKor) {
            list.add(translateService.koToJp(keyword));
            list.add(keyword);
        }
        else if (isJap) list.add(keyword);

        KeywordVariantCache.put("AMZ_" + keyword, list);
        return list;
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
