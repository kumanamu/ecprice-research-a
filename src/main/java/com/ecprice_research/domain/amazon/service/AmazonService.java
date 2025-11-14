package com.ecprice_research.domain.amazon.service;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonService {

    @Value("${SERPAPI_API_KEY}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 아마존 재팬 상품 검색
     * - SerpAPI 엔진 사용
     * - keyword 기반 organic_results 파싱
     * - PriceInfo 구조로 통일
     */
    public PriceInfo search(String keyword) {

        try {
            String url = "https://serpapi.com/search.json"
                    + "?engine=amazon"
                    + "&gl=jp&hl=ja"
                    + "&api_key=" + apiKey
                    + "&k=" + keyword;

            String res = restTemplate.getForObject(url, String.class);
            JSONObject json = new JSONObject(res);

            JSONArray items = json.optJSONArray("organic_results");
            if (items == null || items.isEmpty()) {
                return empty("AMAZON_JP");
            }

            JSONObject first = items.getJSONObject(0);

            long price = 0;
            JSONObject priceObj = first.optJSONObject("price");
            if (priceObj != null) {
                price = priceObj.optLong("value", 0);
            }

            return PriceInfo.builder()
                    .platform("AMAZON_JP")
                    .productName(first.optString("title"))
                    .productUrl(first.optString("link"))
                    .productImage(first.optString("image"))
                    .priceOriginal(price)
                    .shippingOriginal(0)
                    .currencyOriginal("JPY")
                    .build();

        } catch (Exception e) {
            log.error("Amazon API error: {}", e.getMessage());
            return empty("AMAZON_JP");
        }
    }

    private PriceInfo empty(String platform) {
        return PriceInfo.builder()
                .platform(platform)
                .productName("NO_DATA")
                .productUrl("")
                .productImage("")
                .priceOriginal(0)
                .shippingOriginal(0)
                .currencyOriginal("JPY")
                .build();
    }
}
