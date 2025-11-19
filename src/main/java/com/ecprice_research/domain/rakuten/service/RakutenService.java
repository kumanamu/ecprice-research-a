package com.ecprice_research.domain.rakuten.service;

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
public class RakutenService {

    private final TranslateService translateService;
    private final RestTemplate restTemplate = new RestTemplate();

    private final String appId = "1013279438968124438";
    private final String affiliateId = "4e0e3016.41af98ff.4e0e3018.75d936e9";

    public PriceInfo search(String keyword) {

        try {
            String[] variants = buildVariants(keyword);

            for (String k : variants) {

                String url =
                        "https://app.rakuten.co.jp/services/api/IchibaItem/Search/20220601" +
                                "?applicationId=" + appId +
                                "&affiliateId=" + affiliateId +
                                "&keyword=" + k +
                                "&format=json";

                log.info("📡 [Rakuten 요청] {}", url);

                Map<String, Object> res = restTemplate.getForObject(url, Map.class);
                if (res == null || !res.containsKey("Items")) continue;

                List<Map<String, Object>> items = (List<Map<String, Object>>) res.get("Items");
                if (items == null || items.isEmpty()) continue;

                Map<String, Object> item = (Map<String, Object>) items.get(0).get("Item");
                if (item == null) continue;

                String name = String.valueOf(item.get("itemName"));
                String urlStr = String.valueOf(item.get("itemUrl"));
                String image = extractImage(item);
                long price = ((Number) item.get("itemPrice")).longValue();

                return PriceInfo.builder()
                        .platform("RAKUTEN")
                        .productName(name)
                        .productUrl(urlStr)
                        .productImage(image)
                        .priceJpy(price)
                        .currencyOriginal("JPY")
                        .build();
            }

            return error();

        } catch (Exception e) {
            log.error("❌ Rakuten Error: {}", e.getMessage());
            return error();
        }
    }


    private String[] buildVariants(String keyword) {

        String[] cached = KeywordVariantCache.get("RAK_" + keyword);
        if (cached != null) {
            log.info("🔁 [Rakuten 후보 캐시 HIT] {}", Arrays.toString(cached));
            return cached;
        }

        List<String> list = new ArrayList<>();

        boolean isEnglish = keyword.matches("^[a-zA-Z0-9\\s]+$");

        if (isEnglish) {
            list.add(keyword);
        } else {
            String jp = translateService.koToJp(keyword);
            list.add(jp);
        }

        String[] arr = list.toArray(new String[0]);
        KeywordVariantCache.put("RAK_" + keyword, arr);

        log.info("🔍 [Rakuten 검색 후보] {}", Arrays.toString(arr));
        return arr;
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
                .productUrl("")
                .productImage("")
                .priceJpy(0)
                .currencyOriginal("JPY")
                .build();
    }
}
