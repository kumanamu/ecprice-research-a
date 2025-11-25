package com.ecprice_research.domain.naver.service;

import com.ecprice_research.config.NaverConfig;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.translate.service.TranslateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.ecprice_research.domain.keyword.engine.KeywordVariantBuilder;
import com.ecprice_research.keyword.engine.KeywordDetect;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverService {

    private final NaverConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    /** 단일 검색 */
    public PriceInfo search(List<String> keywords) {
        log.info("📡 [Naver] 검색 시작 → '{}'", keywords);
        if (keywords == null || keywords.isEmpty()) {
            return PriceInfo.notFound("NAVER", "No keyword");
        }

        PriceInfo best = null;

        for (String key : keywords) {
            PriceInfo pi = searchSingle(key);
            if (pi == null || !pi.isSuccess()) continue;
            log.warn("❌ [Naver] 검색 실패 → '{}'", keywords);
            if (best == null ||
                    (pi.getPriceKrw() != null &&
                            pi.getPriceKrw() < best.getPriceKrw())) {
                log.info("✅ [Naver] 검색 성공 → {} KRW, {}",
                best = pi);
            }
        }

        return best != null ? best
                : PriceInfo.notFound("NAVER", "Not found");
    }


    private PriceInfo searchSingle(String keywordKR) {
        try {
            String url = config.buildSearchUrl(keywordKR);

            log.info("📡 [Naver API 요청] {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", config.getId());
            headers.set("X-Naver-Client-Secret", config.getSecret());

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            Map body = response.getBody();
            if (body == null) return null;

            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            if (items == null || items.isEmpty()) return null;

            Map<String, Object> item = items.get(0);

            return PriceInfo.builder()
                    .platform("NAVER")
                    .status("SUCCESS")
                    .productName(((String) item.get("title")).replaceAll("<[^>]*>", ""))
                    .productUrl((String) item.get("link"))
                    .productImage((String) item.get("image"))
                    .priceOriginal(Integer.parseInt((String) item.get("lprice")))
                    .currencyOriginal("KRW")
                    .build();

        } catch (Exception e) {
            log.warn("❌ Naver 조회 실패: {}", e.getMessage());
            return null;
        }
    }
}
