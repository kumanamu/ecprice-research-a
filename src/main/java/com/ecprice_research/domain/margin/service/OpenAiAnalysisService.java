package com.ecprice_research.domain.margin.service;

import com.ecprice_research.domain.margin.dto.AiMarginAnalysis;
import com.ecprice_research.domain.margin.dto.MarginCompareResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiAnalysisService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient();

    public AiMarginAnalysis analyze(MarginCompareResult data) {

        try {
            String prompt = """
                    아래는 동일 제품의 국가별 가격 정보이다.
                    환율: 1 JPY = %s KRW
                    
                    Amazon JP: %s KRW
                    Rakuten: %s KRW
                    Naver: %s KRW
                    Coupang: %s KRW
                    
                    위 데이터를 기반으로 아래를 JSON으로만 반환해라.
                    
                    {
                      "buyRecommendation": "",
                      "sellRecommendation": "",
                      "expectedProfitKrw": 0,
                      "expectedProfitRate": 0.0,
                      "reason": ""
                    }
                    """.formatted(
                    data.getKrwToJpy(),
                    data.getAmazonJp().getPriceKrw(),
                    data.getRakuten().getPriceKrw(),
                    data.getNaver().getPriceKrw(),
                    data.getCoupang().getPriceKrw()
            );

            JSONObject json = new JSONObject();
            json.put("model", "gpt-4o-mini");

            json.put("messages", new org.json.JSONArray()
                    .put(new JSONObject().put("role", "system").put("content", "You are a market analyst."))
                    .put(new JSONObject().put("role", "user").put("content", prompt))
            );

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            Request req = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            Response response = client.newCall(req).execute();
            if (!response.isSuccessful()) {
                log.error("AI Error: {}", response.code());
                return null;
            }

            String result = response.body().string();
            JSONObject res = new JSONObject(result)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message");

            JSONObject contentJson = new JSONObject(res.getString("content"));

            return AiMarginAnalysis.builder()
                    .buyRecommendation(contentJson.optString("buyRecommendation"))
                    .sellRecommendation(contentJson.optString("sellRecommendation"))
                    .expectedProfitKrw(contentJson.optLong("expectedProfitKrw"))
                    .expectedProfitRate(contentJson.optDouble("expectedProfitRate"))
                    .reason(contentJson.optString("reason"))
                    .build();

        } catch (Exception e) {
            log.error("AI 분석 실패: {}", e.getMessage());
            return null;
        }
    }
}
