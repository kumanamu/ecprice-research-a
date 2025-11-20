package com.ecprice_research.domain.rakuten.service;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.translate.service.TranslateService;
import com.ecprice_research.util.KeywordVariantCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RakutenService {

    private final TranslateService translateService;
    private final RestTemplate restTemplate = new RestTemplate();

    private final String appId = "1013279438968124438";
    private final String affiliateId = "4e0e3016.41af98ff.4e0e3018.75d936e9";

    // ======================================================================
    // 🔍 메인 엔트리
    // ======================================================================
    public PriceInfo search(String keyword) {
        try {
            List<String> candidates = buildVariants(keyword);
            log.info("🔍 [Rakuten 최종 후보] {}", candidates);

            // 1) API → 2) HTML fallback
            for (String cand : candidates) {
                PriceInfo apiResult = apiSearch(cand);
                if (apiResult != null && apiResult.getPriceJpy() > 0) {
                    return apiResult;
                }

                PriceInfo htmlResult = htmlSearch(cand);
                if (htmlResult != null && htmlResult.getPriceJpy() > 0) {
                    return htmlResult;
                }
            }

            return error();

        } catch (Exception e) {
            log.error("❌ Rakuten Fatal: {}", e.getMessage());
            return error();
        }
    }

    // ======================================================================
    // ✔ 1) API 검색
    // ======================================================================
    private PriceInfo apiSearch(String keyword) {
        try {
            String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

            String url = "https://app.rakuten.co.jp/services/api/IchibaItem/Search/20220601"
                    + "?applicationId=" + appId
                    + "&affiliateId=" + affiliateId
                    + "&keyword=" + encoded
                    + "&format=json";

            log.info("📡 [Rakuten API 요청] {}", url);

            Map<String, Object> res = restTemplate.getForObject(url, Map.class);
            if (res == null || !res.containsKey("Items")) return null;

            List<Map<String, Object>> items =
                    (List<Map<String, Object>>) res.get("Items");

            if (items == null || items.isEmpty()) return null;

            Map<String, Object> itemWrap = items.get(0);
            Map<String, Object> item = (Map<String, Object>) itemWrap.get("Item");

            if (item == null) return null;

            long price = ((Number) item.getOrDefault("itemPrice", 0)).longValue();
            if (price <= 0) return null;

            return PriceInfo.builder()
                    .platform("RAKUTEN")
                    .productName(String.valueOf(item.get("itemName")))
                    .productUrl(String.valueOf(item.get("itemUrl")))
                    .productImage(extractImage(item))
                    .priceJpy(price)
                    .currencyOriginal("JPY")
                    .build();

        } catch (Exception e) {
            log.warn("⚠ Rakuten API 실패 ({}) → HTML fallback", e.getMessage());
            return null;
        }
    }

    // ======================================================================
    // ✔ 2) HTML 직접 파싱 (실제 검색결과 1순위)
    // ======================================================================
    private PriceInfo htmlSearch(String keyword) {
        try {
            String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = "https://search.rakuten.co.jp/search/mall/" + encoded + "/";

            log.info("🌐 [Rakuten HTML 요청] {}", url);

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(5000)
                    .get();

            Element first = doc.selectFirst(".searchresultitem");
            if (first == null) return null;

            String title = first.select(".title").text();
            String link = first.select("a").attr("href");
            String priceText = first.select(".important").text().replaceAll("[^0-9]", "");
            String img = first.select("img").attr("src");

            if (priceText.isBlank()) return null;

            long price = Long.parseLong(priceText);

            log.info("🌐 [Rakuten HTML 상품] {} - {}", title, price);

            return PriceInfo.builder()
                    .platform("RAKUTEN")
                    .productName(title)
                    .productUrl(link)
                    .productImage(img)
                    .priceJpy(price)
                    .currencyOriginal("JPY")
                    .build();

        } catch (Exception e) {
            log.warn("⚠ Rakuten HTML 실패: {}", e.getMessage());
            return null;
        }
    }

    // ======================================================================
    // 후보 키워드 확장 (20개까지)
    // ======================================================================
    private List<String> buildVariants(String keyword) {
        List<String> cached = KeywordVariantCache.get("RAK_" + keyword);
        if (cached != null) return cached;

        List<String> list = new ArrayList<>();

        boolean isEng = keyword.matches("^[a-zA-Z0-9\\s]+$");
        boolean isKor = keyword.matches(".*[가-힣].*");
        boolean isJap = keyword.matches(".*[ぁ-んァ-ン一-龥].*");

        if (isEng) {
            list.add(keyword);
        } else if (isKor) {
            list.add(translateService.koToJp(keyword));
        } else if (isJap) {
            list.add(keyword);
        }

        // 복합어 확장 (일본어 only)
        if (isJap) {
            String base = keyword.replace("の", " ");
            String[] arr = base.split("\\s+");
            list.addAll(Arrays.asList(arr));
        }

        List<String> result = new ArrayList<>(new LinkedHashSet<>(list));
        if (result.size() > 20) result = result.subList(0, 20);

        KeywordVariantCache.put("RAK_" + keyword, result);
        return result;
    }

    private String extractImage(Map<String, Object> item) {
        try {
            Object arr = item.get("mediumImageUrls");
            if (arr instanceof List<?> list && !list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof Map<?, ?> m) {
                    return String.valueOf(m.get("imageUrl"));
                }
            }
        } catch (Exception ignore) {}
        return "";
    }

    private PriceInfo error() {
        return PriceInfo.builder()
                .platform("RAKUTEN")
                .productName("조회 실패")
                .priceJpy(0)
                .productUrl("")
                .productImage("")
                .currencyOriginal("JPY")
                .build();
    }
}
