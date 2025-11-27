package com.ecprice_research.domain.exchange.service;

import com.ecprice_research.domain.exchange.dto.ExchangeRate;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Slf4j
@Service
public class ExchangeService {

    @Value("${exchange.api.key}")
    private String apiKey;

    @Value("${exchange.url.convert}")
    private String convertUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // ============================
    // 🔥 캐싱 필드
    // ============================
    private ExchangeRate cachedRate = null;
    private LocalDateTime lastFetchedAt = null;

    /**
     * 전체 환율 (JPY↔KRW)
     */
    public synchronized ExchangeRate getRate() {

        // 1) 캐시 유효 — 24시간 유지
        if (cachedRate != null && lastFetchedAt != null) {
            if (lastFetchedAt.plusHours(24).isAfter(LocalDateTime.now())) {
                log.info("💾 [환율 캐시 사용] {}", cachedRate);
                return cachedRate;
            }
        }

        // 2) 캐시 만료 → 새로 조회
        log.info("🌐 [환율 API 새 조회]");

        long jpyToKrw = getRate("JPY", "KRW");
        double krwToJpy = getRateDouble("KRW", "JPY");

        ExchangeRate rate = ExchangeRate.builder()
                .jpyToKrw(jpyToKrw)
                .krwToJpy(krwToJpy)
                .build();

        // 캐싱
        cachedRate = rate;
        lastFetchedAt = LocalDateTime.now();

        return rate;
    }

    private long getRate(String from, String to) {
        try {
            String url = "https://api.exchangerate.host/convert?from=" + from +
                    "&to=" + to + "&amount=1" +
                    "&access_key=" + apiKey;

            log.info("💱 환율 API 호출: {}", url);

            String response = restTemplate.getForObject(url, String.class);
            JSONObject json = new JSONObject(response);

            double result = json.optDouble("result", 10);
            return Math.round(result);

        } catch (Exception e) {
            log.error("❌ 환율 조회 실패: {}", e.getMessage());
            return 10;
        }
    }

    private double getRateDouble(String from, String to) {
        try {
            String url = convertUrl +
                    "?from=" + from + "&to=" + to + "&amount=1" +
                    "&api_key=" + apiKey;

            String response = restTemplate.getForObject(url, String.class);
            JSONObject json = new JSONObject(response);

            return json.optDouble("result", 0.1);

        } catch (Exception e) {
            log.error("❌ 환율 조회 실패: {}", e.getMessage());
            return 0.1;
        }
    }
}
