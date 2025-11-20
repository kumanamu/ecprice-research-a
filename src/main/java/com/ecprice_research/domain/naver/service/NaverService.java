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
    private final TranslateService translateService;
    private final RestTemplate restTemplate = new RestTemplate();


    // =====================================================================
    // 🔍 메인 검색
    // =====================================================================
    public PriceInfo search(String keyword) {

        try {
            List<String> variants = buildVariants(keyword);

            for (String k : variants) {

                String url = config.buildSearchUrl(k);
                log.info("📡 [Naver 요청] {}", url);

                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Naver-Client-Id", config.getId());
                headers.set("X-Naver-Client-Secret", config.getSecret());

                ResponseEntity<Map> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Map.class
                );

                Map<String, Object> body = response.getBody();
                if (body == null) continue;

                List<Map<String, Object>> items =
                        (List<Map<String, Object>>) body.get("items");

                if (items == null || items.isEmpty()) continue;

                Map<String, Object> item = items.get(0);

                long price = Long.parseLong(String.valueOf(item.get("lprice")));
                String name = ((String) item.get("title"))
                        .replaceAll("<[^>]*>", "");

                return PriceInfo.builder()
                        .platform("NAVER")
                        .productName(name)
                        .productUrl((String) item.get("link"))
                        .productImage((String) item.get("image"))
                        .priceKrw(price)
                        .currencyOriginal("KRW")
                        .build();
            }

            return error();

        } catch (Exception e) {
            log.error("❌ Naver Error: {}", e.getMessage());
            return error();
        }
    }


    // =====================================================================
    // 🔍 검색 후보 생성 (지침 100% 적용)
    // =====================================================================
    private List<String> buildVariants(String keyword) {

        List<String> cached = KeywordVariantCache.get("NAV_" + keyword);
        if (cached != null) {
            log.info("🔁 [Naver 후보 캐시 HIT] {}", cached);
            return cached;
        }

        List<String> list = new ArrayList<>();

        boolean isEnglish = keyword.matches("^[a-zA-Z0-9\\s\\-_.]+$");
        boolean isKorean  = keyword.matches(".*[가-힣].*");
        boolean isJapanese = keyword.matches(".*[ぁ-んァ-ン一-龥].*");

        // RULE 1: 영어 → 영어 그대로
        if (isEnglish) list.add(keyword);

            // RULE 2: 한국어 → 한국어 그대로
        else if (isKorean) list.add(keyword);

            // RULE 3: 일본어 → 한국어로 번역
        else if (isJapanese) list.add(translateService.jpToKo(keyword));

        List<String> result = KeywordVariantCache.filter(list);
        KeywordVariantCache.put("NAV_" + keyword, result);

        log.info("🔍 [Naver 최종 후보] {}", result);
        return result;
    }


    private PriceInfo error() {
        return PriceInfo.builder()
                .platform("NAVER")
                .productName("조회 실패")
                .productUrl("")
                .productImage("")
                .priceKrw(0)
                .currencyOriginal("KRW")
                .build();
    }
}
