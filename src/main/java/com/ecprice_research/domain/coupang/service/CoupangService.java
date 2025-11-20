package com.ecprice_research.domain.coupang.service;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.translate.service.TranslateService;
import com.ecprice_research.util.KeywordVariantCache;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoupangService {

    @Value("${coupang.accessKey}")
    private String accessKey;

    @Value("${coupang.secretKey}")
    private String secretKey;

    private final TranslateService translateService;

    private static final String DOMAIN = "https://api-gateway.coupang.com";
    private static final String PATH =
            "/v2/providers/affiliate_open_api/apis/openapi/v1/products/search";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();


    // =====================================================================
    // 🔍 메인 검색
    // =====================================================================
    public PriceInfo search(String keyword) {
        try {

            List<String> variants = buildVariants(keyword);

            for (String k : variants) {

                log.info("🔍 [Coupang] 검색 후보: {}", k);

                String encoded = URLEncoder.encode(k, StandardCharsets.UTF_8);
                String uri = PATH + "?keyword=" + encoded;

                String authorization = CoupangSignatureUtil.generate(
                        "GET", uri, secretKey, accessKey
                );

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", authorization);

                ResponseEntity<String> res = restTemplate.exchange(
                        URI.create(DOMAIN + uri),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class
                );

                return parse(res.getBody());
            }

            return error("NO_RESULT");

        } catch (Exception e) {
            log.error("❌ Coupang Error", e);
            return error("EXCEPTION");
        }
    }


    // =====================================================================
    // 🔍 검색 후보 생성 (지침 100% 적용)
    // =====================================================================
    private List<String> buildVariants(String keyword) {

        List<String> cached = KeywordVariantCache.get("CUP_" + keyword);
        if (cached != null) {
            log.info("🔁 [Coupang 후보 캐시 HIT] {}", cached);
            return cached;
        }

        List<String> list = new ArrayList<>();

        boolean isEnglish = keyword.matches("^[a-zA-Z0-9\\s\\-_.]+$");
        boolean isKorean  = keyword.matches(".*[가-힣].*");
        boolean isJapanese = keyword.matches(".*[ぁ-んァ-ン一-龥].*");

        // RULE 1: 영어 → 그대로
        if (isEnglish) list.add(keyword);

            // RULE 2: 한국어 → 그대로
        else if (isKorean) list.add(keyword);

            // RULE 3: 일본어 → 한국어 번역
        else if (isJapanese) list.add(translateService.jpToKo(keyword));

        List<String> result = KeywordVariantCache.filter(list);
        KeywordVariantCache.put("CUP_" + keyword, result);

        log.info("🔍 [Coupang 최종 후보] {}", result);
        return result;
    }


    private PriceInfo parse(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);

            if (!"0".equals(root.path("rCode").asText("")))
                return error("API_ERROR");

            JsonNode data = root.path("data").path("productData");
            if (!data.isArray() || data.isEmpty())
                return error("NO_DATA");

            JsonNode item = data.get(0);

            long price = item.path("productPrice").asLong(0);

            return PriceInfo.builder()
                    .platform("COUPANG")
                    .productName(item.path("productName").asText(""))
                    .productUrl(item.path("productUrl").asText(""))
                    .productImage(item.path("productImage").asText(""))
                    .priceKrw(price)
                    .currencyOriginal("KRW")
                    .build();

        } catch (Exception e) {
            log.error("❌ Coupang Parse Error", e);
            return error("PARSE_ERR");
        }
    }


    private PriceInfo error(String msg) {
        return PriceInfo.builder()
                .platform("COUPANG")
                .productName(msg)
                .productUrl("")
                .productImage("")
                .priceKrw(0)
                .currencyOriginal("KRW")
                .build();
    }
}
