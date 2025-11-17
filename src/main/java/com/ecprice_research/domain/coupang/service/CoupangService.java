package com.ecprice_research.domain.coupang.service;

import com.ecprice_research.domain.coupang.util.CoupangSignatureUtil;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CoupangService {

    @Value("${coupang.accessKey}")
    private String accessKey;

    @Value("${coupang.secretKey}")
    private String secretKey;

    private static final String BASE_URL = "https://api-gateway.coupang.com";

    public List<PriceInfo> search(String keyword) {
        List<PriceInfo> result = new ArrayList<>();

        try {
            // URL 인코딩
            String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

            // HMAC 서명용 전체 경로 (Base URL 생략, 쿼리 포함)
            String fullPath = "/v2/providers/affiliate_open_api/apis/openapi/v1/products/search?keyword=" + encoded;

            // 실제 호출할 URL
            String url = BASE_URL + fullPath;

            log.debug("Calling Coupang API: {}", url);
            log.debug("Access Key: {}", accessKey);

            // ⭐ Timestamp를 미리 생성 (비동기 호출 시 일관성 유지)
            String datetime = CoupangSignatureUtil.generateDateTime();
            // HMAC 서명 생성 (datetime 명시적으로 전달)
            String authorization = CoupangSignatureUtil.generateWithDateTime(
                    "GET",
                    fullPath,
                    secretKey,
                    accessKey,
                    datetime
            );

            log.debug("Generated Authorization: {}", authorization);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorization);
            headers.set("Content-Type", "application/json;charset=UTF-8");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // API 호출
            RestTemplate rt = new RestTemplate();
            ResponseEntity<String> resp = rt.exchange(url, HttpMethod.GET, entity, String.class);

            // 응답 파싱
            JSONObject json = new JSONObject(resp.getBody());

            // rCode 확인
            String rCode = json.optString("rCode");
            if (!"0".equals(rCode)) {
                log.warn("Coupang API Error: {}", json.optString("rMessage"));
                result.add(
                        PriceInfo.builder()
                                .platform("COUPANG")
                                .productName("API_ERROR")
                                .currencyOriginal("KRW")
                                .build()
                );
                return result;
            }

            // data -> productData 배열 추출
            JSONObject dataObj = json.optJSONObject("data");
            if (dataObj == null) {
                result.add(
                        PriceInfo.builder()
                                .platform("COUPANG")
                                .productName("NO_DATA")
                                .currencyOriginal("KRW")
                                .build()
                );
                return result;
            }

            JSONArray items = dataObj.optJSONArray("productData");

            if (items == null || items.isEmpty()) {
                result.add(
                        PriceInfo.builder()
                                .platform("COUPANG")
                                .productName("NO_DATA")
                                .currencyOriginal("KRW")
                                .build()
                );
                return result;
            }

            // 첫 번째 상품만 추출
            JSONObject item = items.getJSONObject(0);

            long price = item.optLong("productPrice", 0);

            PriceInfo info = PriceInfo.builder()
                    .platform("COUPANG")
                    .productName(item.optString("productName"))
                    .productUrl(item.optString("productUrl"))
                    .productImage(item.optString("productImage"))
                    .priceOriginal(price)
                    .priceKrw(price)
                    .currencyOriginal("KRW")
                    .build();

            result.add(info);
            log.info("✅ Coupang search successful: {} ({}원)", info.getProductName(), price);
            return result;

        } catch (Exception e) {
            log.error("❌ Coupang Search Error", e);

            result.add(
                    PriceInfo.builder()
                            .platform("COUPANG")
                            .productName("ERROR")
                            .currencyOriginal("KRW")
                            .build()
            );

            return result;
        }
    }
}