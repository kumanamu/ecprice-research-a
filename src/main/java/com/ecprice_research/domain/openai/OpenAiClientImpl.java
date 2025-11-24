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

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.model}")
    private String model;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    @Override
    public String ask(String prompt) {

        try {
            // 요청 JSON 생성
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
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            String result = response.body().string();

            log.info("🔥 [OpenAI Raw Response] {}", result);
            log.error("🔥 API KEY 체크: {}", apiKey);
            log.error("🔥 MODEL 체크: {}", model);
            JSONObject resJson = new JSONObject(result);

            // 실패 응답 처리
            if (resJson.has("error")) {
                JSONObject err = resJson.getJSONObject("error");
                log.error("❌ OpenAI Error: {}", err.optString("message"));
                return "OpenAI 오류: " + err.optString("message");
            }

            JSONArray choices = resJson.getJSONArray("choices");
            JSONObject message = choices.getJSONObject(0).getJSONObject("message");

            Object rawContent = message.get("content");
            String content = "";

            // 최신 GPT 구조 (배열 기반 content)
            if (rawContent instanceof JSONArray arr) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject c = arr.getJSONObject(i);
                    if ("text".equals(c.optString("type"))) {
                        sb.append(c.optString("text"));
                    }
                }
                content = sb.toString();
            }

            // 예전 구조(content: String)
            else if (rawContent instanceof String str) {
                content = str;
            }

            return content.trim();

        } catch (Exception e) {
            log.error("❌ OpenAI 호출 오류", e);
            return "AI 분석 실패: " + e.getMessage();
        }
    }
}
