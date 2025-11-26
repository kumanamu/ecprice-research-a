package com.ecprice_research.domain.amazon.service;

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
public class AmazonService {

    private final RestTemplate rest = new RestTemplate();
    private final TranslateService translate;

    @Value("${serpapi.api.key}")
    private String serpApiKey;

    public PriceInfo search(String keywordRaw) {
        try {
            // --------------------------
            // 🔥 일본 사이트는 무조건 일본어 검색
            // --------------------------
            String keywordJP = keywordRaw;
            if (keywordRaw.matches(".*[가-힣].*")) {
                keywordJP = translate.koToJp(keywordRaw);
            }

            String encoded = URLEncoder.encode(keywordJP, StandardCharsets.UTF_8);

            String url =
                    "https://serpapi.com/search.json"
                            + "?engine=amazon"
                            + "&amazon_domain=amazon.co.jp"
                            + "&gl=jp"
                            + "&hl=ja"
                            + "&k=" + encoded
                            + "&api_key=" + serpApiKey;

            log.info("📡 [Amazon API 요청] {}", url);

            String json = rest.getForObject(url, String.class);
            if (json == null) {
                return PriceInfo.notFound("AMAZON_JP", "응답 없음");
            }

            JSONObject root = new JSONObject(json);
            JSONArray organic = root.optJSONArray("organic_results");

            if (organic == null || organic.length() == 0) {
                return PriceInfo.notFound("AMAZON_JP", "검색 결과 없음");
            }

            JSONObject best = null;

            // --------------------------
            // 🔥 가격, 썸네일, 링크 fallback 강화
            // --------------------------
            for (int i = 0; i < organic.length(); i++) {

                JSONObject item = organic.getJSONObject(i);

                int price = item.optInt("extracted_price",
                        item.optInt("price", -1));

                if (price <= 0) continue;

                best = item;
                break;
            }

            if (best == null) {
                return PriceInfo.notFound("AMAZON_JP", "유효 상품 없음");
            }

            int priceJPY = best.optInt("extracted_price",
                    best.optInt("price", 0));

            String title = best.optString("title", "상품명 없음");
            String link = best.optString("link_clean",
                    best.optString("link", ""));
            String thumb = best.optString("thumbnail",
                    best.optString("image", ""));

            return PriceInfo.builder()
                    .platform("AMAZON_JP")
                    .productName(title)
                    .productUrl(link)
                    .productImage(thumb)
                    .priceOriginal(priceJPY)
                    .currencyOriginal("JPY")
                    .priceJpy(priceJPY)
                    .status("SUCCESS")
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("❌ Amazon Error: {}", e.getMessage());
            return PriceInfo.notFound("AMAZON_JP", "예외 발생");
        }
    }
}
