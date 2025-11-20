package com.ecprice_research.domain.openai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClientImpl implements OpenAiClient {

    @Value("${OPENAI_API_KEY}")
    private String apiKey;

    @Value("${OPENAI_API_MODEL}")
    private String model;

    // ✅ Timeout 안정화 버전
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    @Override
    public String ask(String prompt) {

        try {
            JSONObject json = new JSONObject();
            json.put("model", model);
            json.put("max_tokens", 600);
            json.put("temperature", 0.2);

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", prompt)
            );
            json.put("messages", messages);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            String result = response.body().string();

            JSONObject resJson = new JSONObject(result);

            return resJson
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

        } catch (Exception e) {
            log.error("❌ OpenAI 호출 오류: {}", e.getMessage());
            return "AI 분석 실패: " + e.getMessage();
        }
    }
}
