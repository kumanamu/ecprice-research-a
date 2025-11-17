package com.ecprice_research.domain.rakuten.service;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class RakutenService {

    @Value("${rakuten.api.key}")
    private String appId;

    @Value("${rakuten.api.affiliate}")
    private String affiliateId;

    public List<PriceInfo> search(String keyword) {
        try {
            String url =
                    "https://app.rakuten.co.jp/services/api/IchibaItem/Search/20170706"
                            + "?applicationId=" + appId
                            + "&affiliateId=" + affiliateId
                            + "&keyword=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8)
                            + "&format=json";

            log.info("📡 [Rakuten API 요청] {}", url);

            RestTemplate rt = new RestTemplate();
            String res = rt.getForObject(url, String.class);
            JSONObject json = new JSONObject(res);

            JSONArray items = json.optJSONArray("Items");
            if (items == null || items.isEmpty()) {
                return List.of(
                        PriceInfo.builder()
                                .platform("RAKUTEN")
                                .productName("NO_DATA")
                                .currencyOriginal("JPY")
                                .build()
                );
            }

            JSONObject item = items.getJSONObject(0).getJSONObject("Item");

            String name = item.optString("itemName", "NO_DATA");
            int price = item.optInt("itemPrice", 0);
            String imgUrl = "";
            try {
                JSONArray images = item.optJSONArray("mediumImageUrls");
                if (images != null && !images.isEmpty()) {
                    imgUrl = images.getJSONObject(0).optString("imageUrl", "");
                }
            } catch (Exception ignored) {}

            PriceInfo info = PriceInfo.builder()
                    .platform("RAKUTEN")
                    .productName(name)
                    .productImage(imgUrl)
                    .productUrl(item.optString("itemUrl", ""))
                    .priceOriginal(price)
                    .currencyOriginal("JPY")
                    .build();

            return List.of(info);

        } catch (Exception e) {
            log.error("❌ [Rakuten] 오류: {}", e.getMessage());
            return List.of(
                    PriceInfo.builder()
                            .platform("RAKUTEN")
                            .productName("NO_DATA")
                            .currencyOriginal("JPY")
                            .build()
            );
        }
    }
}
