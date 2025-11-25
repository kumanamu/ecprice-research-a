package com.ecprice_research.domain.amazon.service;

import com.ecprice_research.domain.keyword.engine.KeywordVariantBuilder;
import com.ecprice_research.keyword.engine.KeywordDetect;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonService {

    private final RestTemplate rest = new RestTemplate();

    @Value("${serpapi.api.key}")
    private String serpApiKey;

  /** 단일 검색 */
  public PriceInfo search(String keyword) {
      {
     log.info("📡 [Amazon] 검색 시작 → '{}'", keyword);

      if (keyword == null || keyword.isEmpty()) {
          return PriceInfo.notFound("AMAZON_JP", "No keyword");
      }

      PriceInfo best = null;

      for (String key : keyword) {
          PriceInfo pi = searchSingle(key);

          if (pi == null || !pi.isSuccess()) continue;
          log.warn("❌ [Amazon] 검색 실패 → '{}'", keyword);
          if (best == null ||
                  (pi.getPriceJpy() != null &&
                          pi.getPriceJpy() < best.getPriceJpy())) {
              log.info("✅ [Amazon] 검색 성공 → {} JPY, {}",
              best = pi);

          }
      }

      return best != null ? best
              : PriceInfo.notFound("AMAZON_JP", "Not found");
  }


    private PriceInfo searchSingle(String keywordJP) {
        try {
            String encoded = URLEncoder.encode(keywordJP, StandardCharsets.UTF_8);

            String url = "https://serpapi.com/search.json"
                    + "?engine=amazon"
                    + "&amazon_domain=amazon.co.jp"
                    + "&gl=jp"
                    + "&hl=ja"
                    + "&k=" + encoded
                    + "&api_key=" + serpApiKey;

            log.info("📡 [Amazon API 요청] {}", url);

            String json = rest.getForObject(url, String.class);
            if (json == null) return null;

            JSONObject root = new JSONObject(json);
            JSONArray organic = root.optJSONArray("organic_results");

            if (organic == null || organic.length() == 0) return null;

            JSONObject best = null;

            for (int i = 0; i < organic.length(); i++) {
                JSONObject item = organic.getJSONObject(i);

                if (!item.has("extracted_price")) continue;

                best = item;
                break;
            }

            if (best == null) return null;

            int priceJPY = best.optInt("extracted_price", -1);
            if (priceJPY <= 0) return null;

            return PriceInfo.builder()
                    .platform("AMAZON_JP")
                    .status("SUCCESS")
                    .productName(best.optString("title"))
                    .productUrl(best.optString("link_clean", best.optString("link", null)))
                    .productImage(best.optString("thumbnail"))
                    .priceOriginal(priceJPY)
                    .currencyOriginal("JPY")
                    .priceJpy(priceJPY)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.warn("❌ Amazon 조회 실패: {}", e.getMessage());
            return null;
        }
    }
}
