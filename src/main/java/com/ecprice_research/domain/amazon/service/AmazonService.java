package com.ecprice_research.domain.amazon.service;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class AmazonService {

    @Value("${serpapi.api.key}")
    private String serpApiKey;

    public List<PriceInfo> search(String keyword) {
        try {
            String url = "https://serpapi.com/search.json"
                    + "?engine=amazon&amazon_domain=amazon.co.jp&gl=jp&hl=ja"
                    + "&k=" + keyword + "&api_key=" + serpApiKey;

            log.info("📡 [Amazon 요청] {}", url);

            RestTemplate rt = new RestTemplate();
            String body = rt.getForObject(url, String.class);

            JSONObject json = new JSONObject(body);
            JSONArray results = json.optJSONArray("organic_results");

            if (results == null || results.isEmpty()) {
                return List.of(
                        PriceInfo.builder()
                                .platform("AMAZON_JP")
                                .productName("NO_DATA")
                                .currencyOriginal("JPY")
                                .build()
                );
            }

            JSONObject item = results.getJSONObject(0);

            String title = item.optString("title");
            String link = item.optString("link");
            String image = item.optString("thumbnail");

            long price = item.optLong("extracted_price", 0);

            if (price == 0) {
                JSONObject variants = item.optJSONObject("variants");
                if (variants != null) {
                    JSONArray options = variants.optJSONArray("options");
                    if (options != null) {
                        long min = Long.MAX_VALUE;
                        for (int i = 0; i < options.length(); i++) {
                            long vp = options.getJSONObject(i).optLong("extracted_price", 0);
                            if (vp > 0 && vp < min) min = vp;
                        }
                        if (min != Long.MAX_VALUE) price = min;
                    }
                }
            }

            PriceInfo info = PriceInfo.builder()
                    .platform("AMAZON_JP")
                    .productName(title)
                    .productUrl(link)
                    .productImage(image)
                    .priceOriginal(price)
                    .currencyOriginal("JPY")
                    .build();

            return List.of(info);

        } catch (Exception e) {
            log.error("❌ Amazon Error: {}", e.getMessage());
            return List.of(
                    PriceInfo.builder()
                            .platform("AMAZON_JP")
                            .productName("ERROR")
                            .currencyOriginal("JPY")
                            .build()
            );
        }
    }
}
