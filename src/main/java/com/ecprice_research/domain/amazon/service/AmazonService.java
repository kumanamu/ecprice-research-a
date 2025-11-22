package com.ecprice_research.domain.amazon.service;

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
public class AmazonService {

    private final RestTemplate rest = new RestTemplate();

    @Value("${serpapi.api.key}")
    private String serpApiKey;

    /**
     * Amazon JP 검색 서비스 (원샷 통합 규칙에 맞춤)
     * - keyword: 이미 토글/번역으로 변환된 "검색용 문자열"
     */
    public PriceInfo search(String keywordJP) {

        try {
            String encoded = URLEncoder.encode(keywordJP, StandardCharsets.UTF_8);

            String url = "https://serpapi.com/search.json"
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

            for (int i = 0; i < organic.length(); i++) {
                JSONObject item = organic.getJSONObject(i);

                // 가격 있는 것만 필터
                if (!item.has("extracted_price")) continue;
                if (!item.has("thumbnail")) continue;

                best = item;
                break;
            }

            if (best == null) {
                return PriceInfo.notFound("AMAZON_JP", "유효 상품 없음");
            }

            int priceJPY = best.optInt("extracted_price", -1);
            if (priceJPY <= 0) {
                return PriceInfo.notFound("AMAZON_JP", "가격 정보 없음");
            }

            String title = best.optString("title", "상품명 없음");
            String link = best.optString("link_clean", best.optString("link", null));
            String thumb = best.optString("thumbnail", null);

            return PriceInfo.builder()
                    .platform("AMAZON_JP")
                    .status("SUCCESS")
                    .productName(title)
                    .productUrl(link)
                    .productImage(thumb)
                    .priceOriginal(priceJPY)
                    .currencyOriginal("JPY")
                    .priceJpy(priceJPY)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.warn("❌ Amazon 조회 실패: {}", e.getMessage());
            return PriceInfo.notFound("AMAZON_JP", "예외 발생");
        }
    }
}
