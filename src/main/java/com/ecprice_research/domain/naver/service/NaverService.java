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

    public PriceInfo search(String keyword) {

        try {
            String[] variants = buildVariants(keyword);

            for (String k : variants) {

                log.info("📡 [Naver 요청] {}", config.buildSearchUrl(k));

                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Naver-Client-Id", config.getId());
                headers.set("X-Naver-Client-Secret", config.getSecret());

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(
                        config.buildSearchUrl(k),
                        HttpMethod.GET,
                        entity,
                        Map.class
                );

                Map<String, Object> body = response.getBody();
                if (body == null) continue;

                List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
                if (items == null || items.isEmpty()) continue;

                Map<String, Object> item = items.get(0);

                long price = Long.parseLong(String.valueOf(item.get("lprice")));
                String name = ((String) item.get("title")).replaceAll("<[^>]*>", "");

                return PriceInfo.builder()
                        .platform("NAVER")
                        .productName(name)
                        .productImage((String) item.get("image"))
                        .productUrl((String) item.get("link"))
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


    private String[] buildVariants(String keyword) {

        String[] cached = KeywordVariantCache.get("NAV_" + keyword);
        if (cached != null) {
            log.info("🔁 [Naver 후보 캐시 HIT] {}", Arrays.toString(cached));
            return cached;
        }

        List<String> list = new ArrayList<>();

        boolean isEnglish = keyword.matches("^[a-zA-Z0-9\\s]+$");
        boolean isKorean  = keyword.matches(".*[가-힣].*");

        if (isEnglish) list.add(keyword);
        else if (isKorean) list.add(keyword);
        else list.add(translateService.jpToKo(keyword)); // 일본어 → 한국어

        String[] arr = list.toArray(new String[0]);
        KeywordVariantCache.put("NAV_" + keyword, arr);

        log.info("🔍 [Naver 검색 후보] {}", Arrays.toString(arr));
        return arr;
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
