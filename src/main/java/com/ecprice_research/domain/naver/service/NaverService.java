package com.ecprice_research.domain.naver.service;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverService {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<PriceInfo> search(String keyword) {

        try {
            String url = "https://openapi.naver.com/v1/search/shop.json?query=" + keyword;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JSONObject json = new JSONObject(resp.getBody());
            JSONArray items = json.optJSONArray("items");
            if (items == null || items.isEmpty()) {
                return List.of(empty());
            }

            JSONObject first = items.getJSONObject(0);

            PriceInfo info = PriceInfo.builder()
                    .platform("NAVER")
                    .productName(first.optString("title"))
                    .productUrl(first.optString("link"))
                    .productImage(first.optString("image"))
                    .priceOriginal(first.optLong("lprice", 0))
                    .shippingOriginal(0)
                    .currencyOriginal("KRW")
                    .build();

            return List.of(info);

        } catch (Exception e) {
            log.error("Naver API error: {}", e.getMessage());
            return List.of(empty());
        }
    }

    private PriceInfo empty() {
        return PriceInfo.builder()
                .platform("NAVER")
                .productName("NO_DATA")
                .priceOriginal(0)
                .currencyOriginal("KRW")
                .build();
    }
}
