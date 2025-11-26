package com.ecprice_research.domain.naver.service;

import com.ecprice_research.config.NaverConfig;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.translate.service.TranslateService;
import com.ecprice_research.util.KeywordVariantCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverService {

    private final NaverConfig config;
    private final TranslateService translate;
    private final RestTemplate rest = new RestTemplate();

    public PriceInfo search(String keyword) {

        try {
            // 한글 그대로, 일본어면 → 한국어 변환
            if (keyword.matches(".*[ぁ-んァ-ン一-龥].*")) {
                keyword = translate.jpToKo(keyword);
            }

            String url = config.buildSearchUrl(keyword);
            log.info("📡 [Naver 요청] {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", config.getId());
            headers.set("X-Naver-Client-Secret", config.getSecret());

            ResponseEntity<Map> response = rest.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null) return PriceInfo.notFound("NAVER", "응답 없음");

            List<Map<String, Object>> list = (List<Map<String, Object>>) body.get("items");
            if (list == null || list.isEmpty()) return PriceInfo.notFound("NAVER", "검색 없음");

            Map<String, Object> item = list.get(0);

            String name = ((String) item.get("title")).replaceAll("<[^>]*>", "");
            int price = Integer.parseInt((String) item.get("lprice"));

            return PriceInfo.builder()
                    .platform("NAVER")
                    .productName(name)
                    .productUrl((String) item.get("link"))
                    .productImage((String) item.get("image"))
                    .priceOriginal(price)
                    .currencyOriginal("KRW")
                    .priceKrw(price)
                    .status("SUCCESS")
                    .build();

        } catch (Exception e) {
            log.error("❌ Naver Error: {}", e.getMessage());
            return PriceInfo.notFound("NAVER", "예외 발생");
        }
    }
}
