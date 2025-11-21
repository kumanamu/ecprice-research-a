package com.ecprice_research.domain.amazon.service;

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
public class AmazonService {

    private final RestTemplate rest = new RestTemplate();

    private final String SERP_API_KEY = "d7c0dd0ccb16661ed77b37d9e9395ba00646d03ca0f35b72608faf9661253511";

    public PriceInfo search(String keyword) {

        try {
            String k = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = "https://serpapi.com/search.json?engine=amazon&amazon_domain=amazon.co.jp&gl=jp&hl=ja&k="
                    + k + "&api_key=" + SERP_API_KEY;

            log.info("📡 [Amazon API 요청] {}", url);

            String json = rest.getForObject(url, String.class);

            JSONObject root = new JSONObject(json);
            JSONArray organic = root.optJSONArray("organic_results");

            if (organic == null || organic.isEmpty()) {
                return PriceInfo.notFound("AMAZON_JP", "검색 결과 없음(organic empty)");
            }

            // 후보 필터링
            JSONObject best = null;
            for (int i = 0; i < organic.length(); i++) {
                JSONObject obj = organic.getJSONObject(i);

                if (!obj.has("extracted_price")) continue;
                if (!obj.has("thumbnail")) continue;
                if (!obj.has("asin")) continue;

                best = obj;
                break;
            }

            if (best == null) {
                return PriceInfo.notFound("AMAZON_JP", "유효한 상품 없음(필터 조건 불일치)");
            }

            int price = best.optInt("extracted_price", -1);
            if (price <= 0) {
                return PriceInfo.notFound("AMAZON_JP", "가격 없음");
            }

            String title = best.optString("title", "상품명 없음");
            String link = best.optString("link_clean", best.optString("link", null));
            String thumb = best.optString("thumbnail", null);

            int jpy = price;
            int krw = (int)(jpy * 9); // 환율은 MarginService에서 다시 변환됨

            return PriceInfo.builder()
                    .platform("AMAZON_JP")
                    .status("SUCCESS")
                    .productName(title)
                    .productUrl(link)
                    .productImage(thumb)
                    .priceOriginal(jpy)
                    .shippingOriginal(0)
                    .currencyOriginal("JPY")
                    .priceJpy(jpy)
                    .priceKrw(krw)
                    .displayPrice(jpy + " JPY")
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.warn("❌ Amazon 조회 실패: {}", e.getMessage());
            return PriceInfo.notFound("AMAZON_JP", "예외 발생");
        }
    }
}
