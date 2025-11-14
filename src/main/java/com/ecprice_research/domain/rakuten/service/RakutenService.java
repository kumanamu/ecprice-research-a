package com.ecprice_research.domain.rakuten.service;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;

@Service
@RequiredArgsConstructor
@Slf4j
public class RakutenService {

    @Value("${rakuten.api.key}")
    private String appId;

    @Value("${rakuten.api.affiliate}")
    private String affiliateId;

    public PriceInfo search(String keyword) {
        try {
            String url = "https://app.rakuten.co.jp/services/api/IchibaItem/Search/20220601"
                    + "?applicationId=" + appId
                    + "&keyword=" + URLEncoder.encode(keyword, "UTF-8")
                    + "&hits=1";

            RestTemplate rt = new RestTemplate();
            String res = rt.getForObject(url, String.class);

            JSONObject json = new JSONObject(res);
            var items = json.optJSONArray("Items");

            if (items == null || items.isEmpty()) {
                return PriceInfo.builder()
                        .platform("RAKUTEN")
                        .productName("NO_DATA")
                        .currencyOriginal("JPY")
                        .build();
            }

            JSONObject item = items.getJSONObject(0).getJSONObject("Item");

            return PriceInfo.builder()
                    .platform("RAKUTEN")
                    .productName(item.optString("itemName"))
                    .productUrl(item.optString("itemUrl"))
                    .productImage(item.optString("mediumImageUrls").replace("[", "").replace("]", ""))
                    .priceOriginal(item.optLong("itemPrice", 0))
                    .shippingOriginal(0)
                    .currencyOriginal("JPY")
                    .build();

        } catch (Exception e) {
            log.error("Rakuten API error: {}", e.getMessage());
            return PriceInfo.builder()
                    .platform("RAKUTEN")
                    .productName("ERROR")
                    .currencyOriginal("JPY")
                    .build();
        }
    }
}
