package com.ecprice_research.domain.rakuten.service;

import com.ecprice_research.domain.margin.dto.PriceInfo;
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

    @Value("${rakuten.api.key}")
    private String appId;

    @Value("${rakuten.api.affiliate}")
    private String affiliateId;

    @Value("${rakuten.api.apiUrl}")
    private String rakutenApiUrl;

    /**
     * Rakuten 검색 (통합 설계에 맞춤)
     */
    public PriceInfo search(String keywordJP) {

        try {
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
                return PriceInfo.notFound("RAKUTEN", "검색 결과 없음");
            }

            JSONObject bestItem = null;
            int bestPrice = Integer.MAX_VALUE;

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i).getJSONObject("Item");

                int price = item.optInt("itemPrice", -1);
                if (price <= 0) continue;

                // 하드코딩 필터 제거됨
                if (price < bestPrice) {
                    bestPrice = price;
                    bestItem = item;
                }
            }

            if (bestItem == null) {
                return PriceInfo.notFound("RAKUTEN", "유효한 상품 없음");
            }

            String title = bestItem.optString("itemName");
            String link = bestItem.optString("itemUrl");
            JSONArray imgs = bestItem.optJSONArray("mediumImageUrls");
            String img = (imgs != null && imgs.length() > 0)
                    ? imgs.getJSONObject(0).optString("imageUrl")
                    : null;

            return PriceInfo.builder()
                    .platform("RAKUTEN")
                    .status("SUCCESS")
                    .productName(title)
                    .productUrl(link)
                    .productImage(img)
                    .priceOriginal(bestPrice)
                    .currencyOriginal("JPY")
                    .priceJpy(bestPrice)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.warn("❌ Rakuten 조회 실패: {}", e.getMessage());
            return PriceInfo.notFound("RAKUTEN", "예외 발생");
        }
    }
}
