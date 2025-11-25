package com.ecprice_research.domain.rakuten.service;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.ecprice_research.domain.keyword.engine.KeywordVariantBuilder;
import com.ecprice_research.keyword.engine.KeywordDetect;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

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

    /** 단일 검색 */
    public PriceInfo search(List<String> keywords) {
        log.info("📡 [Rakuten] 검색 시작 → '{}'", keywords);
        if (keywords == null || keywords.isEmpty()) {
            return PriceInfo.notFound("RAKUTEN", "No keyword");
        }

        PriceInfo best = null;

        for (String key : keywords) {
            PriceInfo pi = searchSingle(key);
            if (pi == null || !pi.isSuccess()) continue;
            log.warn("❌ [Rakuten] 검색 실패 → '{}'", keywords);
            if (best == null ||
                    (pi.getPriceJpy() != null &&
                            pi.getPriceJpy() < best.getPriceJpy())) {
                log.info("✅ [Amazon] 검색 성공 → {} JPY, {}",
                        best = pi);
            }

        }

        return best != null ? best
                : PriceInfo.notFound("RAKUTEN", "Not found");
    }


    private PriceInfo searchSingle(String keywordJP) {
        try {
            String encoded = URLEncoder.encode(keywordJP, StandardCharsets.UTF_8);

            String url = rakutenApiUrl
                    + "?applicationId=" + appId
                    + "&affiliateId=" + affiliateId
                    + "&keyword=" + encoded;

            log.info("📡 [Rakuten API 요청] {}", url);

            String json = rest.getForObject(url, String.class);
            if (json == null) return null;

            JSONObject root = new JSONObject(json);
            JSONArray items = root.optJSONArray("Items");

            if (items == null || items.length() == 0) return null;

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

            if (bestItem == null) return null;

            JSONArray imgs = bestItem.optJSONArray("mediumImageUrls");
            String img = (imgs != null && imgs.length() > 0)
                    ? imgs.getJSONObject(0).optString("imageUrl")
                    : null;

            return PriceInfo.builder()
                    .platform("RAKUTEN")
                    .status("SUCCESS")
                    .productName(bestItem.optString("itemName"))
                    .productUrl(bestItem.optString("itemUrl"))
                    .productImage(img)
                    .priceOriginal(bestPrice)
                    .currencyOriginal("JPY")
                    .priceJpy(bestPrice)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.warn("❌ Rakuten 조회 실패: {}", e.getMessage());
            return null;
        }
    }
}
