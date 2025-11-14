package com.ecprice_research.domain.coupang.service;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoupangService {

    @Value("${coupang.partnerId}")
    private String partnerId;

    @Value("${coupang.accessKey}")
    private String accessKey;

    @Value("${coupang.secretKey}")
    private String secretKey;

    @Value("${coupang.apiUrl}")
    private String apiUrl;

    private static final String REQUEST_METHOD = "GET";

    public PriceInfo search(String keyword) {

        try {
            long timestamp = System.currentTimeMillis();

            // ========= Signature 생성 =========
            String message = REQUEST_METHOD + " " + "/v2/providers/affiliate_open_api/apis/openapi/products/search" + "\n" + timestamp + "\n" + partnerId;

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String signature = Base64.getEncoder().encodeToString(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));

            // ========= URL 생성 =========
            String requestUrl = apiUrl
                    + "?keyword=" + URLEncoder.encode(keyword, "UTF-8")
                    + "&limit=1";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "CEA " + partnerId + ":" + signature);
            headers.set("X-Requested-Timestamp", String.valueOf(timestamp));
            headers.set("X-Coupang-Api-Version", "2");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate rt = new RestTemplate();
            ResponseEntity<String> res = rt.exchange(requestUrl, HttpMethod.GET, entity, String.class);

            JSONObject json = new JSONObject(res.getBody());
            var data = json.optJSONArray("data");

            if (data == null || data.isEmpty()) {
                return PriceInfo.builder()
                        .platform("COUPANG")
                        .productName("NOT_FOUND: " + keyword)
                        .currencyOriginal("KRW")
                        .build();
            }

            JSONObject item = data.getJSONObject(0);

            return PriceInfo.builder()
                    .platform("COUPANG")
                    .productName(item.optString("productName"))
                    .productUrl(item.optString("productUrl"))
                    .productImage(item.optString("productImage"))
                    .priceOriginal(item.optLong("price", 0))
                    .shippingOriginal(item.optLong("deliveryFee", 0))
                    .currencyOriginal("KRW")
                    .build();

        } catch (Exception e) {
            log.error("Coupang API error: {}", e.getMessage());
            return PriceInfo.builder()
                    .platform("COUPANG")
                    .productName("ERROR")
                    .currencyOriginal("KRW")
                    .build();
        }
    }
}
