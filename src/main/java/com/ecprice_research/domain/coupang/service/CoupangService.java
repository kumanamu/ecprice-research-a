package com.ecprice_research.domain.coupang.service;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.translate.service.TranslateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.ecprice_research.domain.keyword.engine.KeywordVariantBuilder;
import com.ecprice_research.keyword.engine.KeywordDetect;

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

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    private static final String DOMAIN = "https://api-gateway.coupang.com";
    private static final String PATH =
            "/v2/providers/affiliate_open_api/apis/openapi/v1/products/search";

    /** 단일 검색 */
    public PriceInfo search(List<String> keywords) {
        log.info("📡 [Coupang] 검색 시작 → '{}'", keywords);
        if (keywords == null || keywords.isEmpty()) {
            return PriceInfo.notFound("COUPANG", "No keyword");
        }

        PriceInfo best = null;

        for (String key : keywords) {
            PriceInfo pi = searchSingle(key);
            if (pi == null || !pi.isSuccess()) continue;
            log.warn("❌ [Coupang] 검색 실패 → '{}'", keywords);
            if (best == null ||
                    (pi.getPriceKrw() != null &&
                            pi.getPriceKrw() < best.getPriceKrw())) {
                log.info("✅ [Coupang] 검색 성공 → {} KRW, {}",
                best =pi);
            }
        }

        return best != null ? best
                : PriceInfo.notFound("COUPANG", "Not found");
    }



    private PriceInfo searchSingle(String keywordKR) {
        try {
            String encoded = URLEncoder.encode(keywordKR, StandardCharsets.UTF_8);
            String uri = PATH + "?keyword=" + encoded;

            String authorization = CoupangSignatureUtil.generate(
                    "GET",
                    uri,
                    secretKey,
                    accessKey
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorization);

            ResponseEntity<String> response = restTemplate.exchange(
                    URI.create(DOMAIN + uri),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            return parse(response.getBody());

        } catch (Exception e) {
            log.warn("❌ Coupang 조회 실패: {}", e.getMessage());
            return null;
        }
    }

    private PriceInfo parse(String json) {
        try {
            JsonNode root = om.readTree(json);

            if (!"0".equals(root.path("rCode").asText()))
                return null;

            JsonNode data = root.path("data").path("productData");
            if (data.isMissingNode() || !data.isArray() || data.isEmpty())
                return null;

            JsonNode item = data.get(0);

            return PriceInfo.builder()
                    .platform("COUPANG")
                    .status("SUCCESS")
                    .productName(item.path("productName").asText(""))
                    .productUrl(item.path("productUrl").asText(""))
                    .productImage(item.path("productImage").asText(""))
                    .priceOriginal(item.path("productPrice").asInt(0))
                    .currencyOriginal("KRW")
                    .build();

        } catch (Exception e) {
            return null;
        }
    }
}
