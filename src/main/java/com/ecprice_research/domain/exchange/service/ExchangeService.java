package com.ecprice_research.domain.exchange.service;

import com.ecprice_research.domain.exchange.dto.ExchangeRate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeService {

    @Value("${exchange.api.key}")
    private String apiKey;

    @Value("${exchange.url.convert}")
    private String convertUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 전체 환율 (JPY↔KRW)
     */
    public ExchangeRate getRate() {

        long jpyToKrw = getRate("JPY", "KRW");
        double krwToJpy = getRateDouble("KRW", "JPY");

        return ExchangeRate.builder()
                .jpyToKrw(jpyToKrw)
                .krwToJpy(krwToJpy)
                .build();
    }

    private long getRate(String from, String to) {
        try {
            String url = convertUrl +
                    "?from=" + from + "&to=" + to + "&amount=1" +
                    "&api_key=" + apiKey;

            JSONObject json = new JSONObject(restTemplate.getForObject(url, String.class));
            return Math.round(json.optDouble("result", 0));

        } catch (Exception e) {
            log.error("Exchange error: {}", e.getMessage());
            return 10;
        }
    }

    private double getRateDouble(String from, String to) {
        try {
            String url = convertUrl +
                    "?from=" + from + "&to=" + to + "&amount=1" +
                    "&api_key=" + apiKey;

            JSONObject json = new JSONObject(restTemplate.getForObject(url, String.class));
            return json.optDouble("result", 0.1);

        } catch (Exception e) {
            log.error("Exchange error: {}", e.getMessage());
            return 0.1;
        }
    }
}
