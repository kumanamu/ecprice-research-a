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

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public PriceInfo search(String keyword) {

        try {
            List<String> variants = buildVariants(keyword);

            for (String k : variants) {

                log.info("🔍 [Coupang 후보] {}", k);

                String encoded = URLEncoder.encode(k, StandardCharsets.UTF_8);
                String uri = PATH + "?keyword=" + encoded;

                String authorization = CoupangSignatureUtil.generate(
                        "GET", uri, secretKey, accessKey
                );

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", authorization);

                ResponseEntity<String> res = rest.exchange(
                        URI.create(DOMAIN + uri),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class
                );

                return parse(res.getBody());
            }

            return PriceInfo.notFound("COUPANG", "NO_RESULT");

        } catch (Exception e) {
            log.error("❌ Coupang Error", e);
            return PriceInfo.notFound("COUPANG", "EXCEPTION");
        }
    }

    private List<String> buildVariants(String keyword) {

        List<String> cached = KeywordVariantCache.get("CUP_" + keyword);
        if (cached != null) {
            log.info("🔁 [CUP 캐시 HIT] {}", cached);
            return cached;
        }

        List<String> list = new ArrayList<>();

        boolean isEnglish = keyword.matches("^[a-zA-Z0-9\\s\\-_.]+$");
        boolean isKorean = keyword.matches(".*[가-힣].*");
        boolean isJapanese = keyword.matches(".*[ぁ-んァ-ン一-龥].*");

        if (isEnglish) list.add(keyword);
        else if (isKorean) list.add(keyword);
        else if (isJapanese) list.add(translateService.jpToKo(keyword));

        List<String> result = KeywordVariantCache.filter(list);
        KeywordVariantCache.put("CUP_" + keyword, result);

        return result;
    }

    private PriceInfo parse(String json) {

        try {
            JsonNode root = mapper.readTree(json);

            if (!"0".equals(root.path("rCode").asText()))
                return PriceInfo.notFound("COUPANG", "API_ERROR");

            JsonNode arr = root.path("data").path("productData");
            if (!arr.isArray() || arr.isEmpty())
                return PriceInfo.notFound("COUPANG", "NO_DATA");

            JsonNode item = arr.get(0);

            long price = item.path("productPrice").asLong(0);
            if (price <= 0)
                return PriceInfo.notFound("COUPANG", "INVALID_PRICE");

            return PriceInfo.builder()
                    .platform("COUPANG")
                    .status("SUCCESS")
                    .productName(item.path("productName").asText())
                    .productUrl(item.path("productUrl").asText())
                    .productImage(item.path("productImage").asText())
                    .priceOriginal((int) price)  // KRW
                    .currencyOriginal("KRW")
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("❌ Coupang Parse Error", e);
            return PriceInfo.notFound("COUPANG", "PARSE_EXCEPTION");
        }
    }
}
