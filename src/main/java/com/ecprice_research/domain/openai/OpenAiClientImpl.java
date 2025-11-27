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
            // 최신 OpenAI 규격: max_tokens / temperature 제거
            JSONObject json = new JSONObject();
            json.put("model", model);

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

            log.info("🔥 [OpenAI Raw Response] {}", result);

            JSONObject resJson = new JSONObject(result);

            if (resJson.has("error")) {
                JSONObject err = resJson.getJSONObject("error");
                log.error("❌ OpenAI Error: {}", err.toString());
                return "OpenAI 오류: " + err.optString("message");
            }

            JSONArray choices = resJson.optJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return "AI 응답 없음";
            }

            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            Object rawContent = message.get("content");

            // 최신 구조는 content 배열
            if (rawContent instanceof JSONArray arr) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.getJSONObject(i);
                    if ("text".equals(item.optString("type"))) {
                        sb.append(item.optString("text"));
                    }
                }
                return sb.toString().trim();
            }

            // 예전 구조는 String
            if (rawContent instanceof String s) {
                return s.trim();
            }

            return "AI 응답 파싱 실패";

        } catch (Exception e) {
            log.error("❌ OpenAI 호출 오류", e);
            return "AI 분석 실패: " + e.getMessage();
        }
    }
}
