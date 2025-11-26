package com.ecprice_research.domain.rakuten.service;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.translate.service.TranslateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class RakutenService {

    private final RestTemplate rest = new RestTemplate();
    private final TranslateService translate;

    @Value("${rakuten.api.key}")
    private String appId;

    @Value("${rakuten.api.affiliate}")
    private String affiliateId;

    @Value("${rakuten.api.apiUrl}")
    private String rakutenApiUrl;

    public PriceInfo search(String keywordRaw) {

        try {
            // 🔥 일본 사이트 → 일본어로 강제 변환
            String keywordJP = keywordRaw;
            if (keywordRaw.matches(".*[가-힣].*")) {
                keywordJP = translate.koToJp(keywordRaw);
            }

            String encoded = URLEncoder.encode(keywordJP, StandardCharsets.UTF_8);

            String url = rakutenApiUrl
                    + "?applicationId=" + appId
                    + "&affiliateId=" + affiliateId
                    + "&keyword=" + encoded
                    + "&format=json";

            log.info("📡 [Rakuten API 요청] {}", url);

            String json = rest.getForObject(url, String.class);
            if (json == null) {
                return PriceInfo.notFound("RAKUTEN", "응답 없음");
            }

            JSONObject root = new JSONObject(json);
            JSONArray items = root.optJSONArray("Items");

            if (items == null || items.length() == 0) {
                return PriceInfo.notFound("RAKUTEN", "검색 없음");
            }

            JSONObject bestItem = null;
            int bestPrice = Integer.MAX_VALUE;

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i).getJSONObject("Item");

                int price = item.optInt("itemPrice", -1);
                if (price <= 0) continue;

                if (price < bestPrice) {
                    bestPrice = price;
                    bestItem = item;
                }
            }

            if (bestItem == null) {
                return PriceInfo.notFound("RAKUTEN", "유효 상품 없음");
            }

            String title = bestItem.optString("itemName");
            String link = bestItem.optString("itemUrl");

            JSONArray imgs = bestItem.optJSONArray("mediumImageUrls");
            String img = (imgs != null && imgs.length() > 0)
                    ? imgs.getJSONObject(0).optString("imageUrl")
                    : "";

            return PriceInfo.builder()
                    .platform("RAKUTEN")
                    .productName(title)
                    .productUrl(link)
                    .productImage(img)
                    .priceOriginal(bestPrice)
                    .currencyOriginal("JPY")
                    .priceJpy(bestPrice)
                    .status("SUCCESS")
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("❌ Rakuten Error: {}", e.getMessage());
            return PriceInfo.notFound("RAKUTEN", "예외 발생");
        }
    }
}
