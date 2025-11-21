package com.ecprice_research.domain.rakuten.service;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class RakutenService {

    private final RestTemplate rest = new RestTemplate();

    private final String APP_ID = "1013279438968124438";
    private final String AFF_ID = "4e0e3016.41af98ff.4e0e3018.75d936e9";

    public PriceInfo search(String keyword) {

        try {
            String k = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

            String url = "https://app.rakuten.co.jp/services/api/IchibaItem/Search/20220601"
                    + "?applicationId=" + APP_ID
                    + "&affiliateId=" + AFF_ID
                    + "&keyword=" + k
                    + "&format=json";

            log.info("📡 [Rakuten API 요청] {}", url);

            String json = rest.getForObject(url, String.class);

            JSONObject root = new JSONObject(json);
            JSONArray items = root.getJSONArray("Items");

            JSONObject bestItem = null;
            int bestPrice = Integer.MAX_VALUE;

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i).getJSONObject("Item");

                int price = item.optInt("itemPrice", -1);
                if (price <= 0) continue;

                String name = item.optString("itemName", "");

                // 키워드가 상품명에 포함된 것만 선택
                if (!name.contains(keyword) && !name.contains("キムチ")) continue;

                if (price < bestPrice) {
                    bestPrice = price;
                    bestItem = item;
                }
            }

            if (bestItem == null) {
                return PriceInfo.notFound("RAKUTEN", "유효한 상품 없음");
            }

            String title = bestItem.optString("itemName");
            String url2 = bestItem.optString("itemUrl");
            JSONArray imgs = bestItem.optJSONArray("mediumImageUrls");
            String img = (imgs != null && imgs.length() > 0)
                    ? imgs.getJSONObject(0).optString("imageUrl")
                    : null;

            int jpy = bestPrice;
            int krw = (int)(jpy * 9);

            return PriceInfo.builder()
                    .platform("RAKUTEN")
                    .status("SUCCESS")
                    .productName(title)
                    .productUrl(url2)
                    .productImage(img)
                    .priceOriginal(jpy)
                    .currencyOriginal("JPY")
                    .priceKrw(krw)
                    .priceJpy(jpy)
                    .displayPrice(jpy + " JPY")
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.warn("❌ Rakuten 조회 실패: {}", e.getMessage());
            return PriceInfo.notFound("RAKUTEN", "예외 발생");
        }
    }
}
