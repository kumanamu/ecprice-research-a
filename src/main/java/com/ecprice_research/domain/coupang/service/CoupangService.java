package com.ecprice_research.domain.coupang.service;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.util.KeywordVariantCache;
import com.ecprice_research.domain.translate.service.TranslateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
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
    private static final String PATH = "/v2/providers/affiliate_open_api/apis/openapi/v1/products/search";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();



    public PriceInfo search(String keyword) {

        try {
            String[] variants = buildVariants(keyword);

            for (String k : variants) {

                log.info("🔍 [Coupang] 검색 후보: {}", k);

                String encodedKeyword = URLEncoder.encode(k, StandardCharsets.UTF_8);
                String uri = PATH + "?keyword=" + encodedKeyword;

                String authorization = CoupangSignatureUtil.generate(
                        "GET",
                        uri,
                        secretKey,
                        accessKey
                );

                URI fullUri = URI.create(DOMAIN + uri);

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", authorization);

                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        fullUri,
                        HttpMethod.GET,
                        entity,
                        String.class
                );

                return parse(response.getBody(), k);
            }

            return error("NO_RESULT");

        } catch (Exception e) {
            log.error("❌ Coupang Error", e);
            return error("EXCEPTION");
        }
    }


    private String[] buildVariants(String keyword) {

        String[] cached = KeywordVariantCache.get("CUP_" + keyword);
        if (cached != null) {
            log.info("🔁 [Coupang 후보 캐시 HIT] {}", Arrays.toString(cached));
            return cached;
        }

        List<String> list = new ArrayList<>();

        boolean isEnglish = keyword.matches("^[a-zA-Z0-9\\s]+$");
        boolean isKorean  = keyword.matches(".*[가-힣].*");

        if (isEnglish) list.add(keyword);
        else if (isKorean) list.add(keyword);
        else list.add(translateService.jpToKo(keyword)); // 일본어 → 한국어

        String[] arr = list.toArray(new String[0]);
        KeywordVariantCache.put("CUP_" + keyword, arr);

        log.info("🔍 [Coupang 검색 후보] {}", Arrays.toString(arr));
        return arr;
    }


    private PriceInfo parse(String responseBody, String keyword) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            String rCode = root.path("rCode").asText("");
            if (!"0".equals(rCode)) return error("API_ERROR");

            JsonNode data = root.path("data").path("productData");
            if (!data.isArray() || data.isEmpty()) return error("NO_DATA");

            JsonNode item = data.get(0);

            String name = item.path("productName").asText("Unknown");
            long price = item.path("productPrice").asLong(0);

            return PriceInfo.builder()
                    .platform("COUPANG")
                    .productName(name)
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


    private static class LoggingInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution exec)
                throws IOException {

            System.out.println("=== [ACTUAL HTTP REQUEST] ===");
            System.out.println("URI: " + request.getURI());
            System.out.println("Method: " + request.getMethod());
            request.getHeaders().forEach((k,v)-> System.out.println(k+"="+v));
            System.out.println("=============================");

            return exec.execute(request, body);
        }
    }
}
