package com.ecprice_research.domain.translate.service;

import com.ecprice_research.domain.translate.dto.TranslateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import okhttp3.*;
import org.json.JSONObject;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslateService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient();

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    /**
     * 한국어 → 일본어
     */
    public TranslateResponse translateKoToJa(String text) {
        return translate(text, "ko", "ja");
    }

    /**
     * 일본어 → 한국어
     */
    public TranslateResponse translateJaToKo(String text) {
        return translate(text, "ja", "ko");
    }


    /**
     * 범용 번역 메서드
     */
    private TranslateResponse translate(String text, String source, String target) {
        try {
            JSONObject json = new JSONObject();
            json.put("model", "gpt-4o-mini");

            JSONArray messages = new JSONArray();

            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content",
                    "You are a professional high-accuracy translation engine. "
                            + "Translate the user's text from " + source + " to " + target
                            + " without adding or removing meaning.");
            messages.put(system);

            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("content", text);
            messages.put(user);

            json.put("messages", messages);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(OPENAI_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                throw new RuntimeException("OpenAI API Error: " + response.code());
            }

            String res = response.body().string();
            JSONObject resJson = new JSONObject(res);
            String translated = resJson
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            return TranslateResponse.builder()
                    .originalText(text)
                    .translatedText(translated.trim())
                    .sourceLang(source)
                    .targetLang(target)
                    .build();

        } catch (Exception e) {
            log.error("Translation error: {}", e.getMessage());
            return TranslateResponse.builder()
                    .originalText(text)
                    .translatedText("Translation failed.")
                    .sourceLang(source)
                    .targetLang(target)
                    .build();
        }
    }
}
