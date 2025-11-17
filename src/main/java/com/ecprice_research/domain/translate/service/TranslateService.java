package com.ecprice_research.domain.translate.service;

import com.ecprice_research.domain.translate.dto.TranslateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import okhttp3.*;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslateService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient();

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    /** 🔥 캐싱 추가: 동일 문장 반복 번역 방지 */
    private final Map<String, String> cache = new ConcurrentHashMap<>();

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
     * 🔥 범용 번역 API (429 방지 + 캐싱 적용)
     */
    private TranslateResponse translate(String text, String source, String target) {

        try {
            String cacheKey = source + ":" + target + ":" + text;

            // 1) 캐시 먼저 조회 (속도↑ 비용↓)
            if (cache.containsKey(cacheKey)) {
                return TranslateResponse.builder()
                        .originalText(text)
                        .translatedText(cache.get(cacheKey))
                        .sourceLang(source)
                        .targetLang(target)
                        .build();
            }

            // 2) OpenAI 요청 구성
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

            // 3) 429 처리 — 재시도 금지
            if (response.code() == 429) {
                log.error("❌ OpenAI 429 Too Many Requests — 번역 스킵, 원문 반환");
                return TranslateResponse.builder()
                        .originalText(text)
                        .translatedText(text)
                        .sourceLang(source)
                        .targetLang(target)
                        .build();
            }

            if (!response.isSuccessful()) {
                throw new RuntimeException("OpenAI API Error: " + response.code());
            }

            // 4) 정상 번역
            String res = response.body().string();
            JSONObject resJson = new JSONObject(res);
            String translated = resJson
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim();

            // 5) 캐시에 저장
            cache.put(cacheKey, translated);

            return TranslateResponse.builder()
                    .originalText(text)
                    .translatedText(translated)
                    .sourceLang(source)
                    .targetLang(target)
                    .build();

        } catch (Exception e) {
            log.error("Translation error: {}", e.getMessage());
            return TranslateResponse.builder()
                    .originalText(text)
                    .translatedText(text)  // fallback
                    .sourceLang(source)
                    .targetLang(target)
                    .build();
        }
    }

    /**
     * 🔥 언어 토글 기반 번역기
     */
    public String translateText(String text, String targetLang) {
        try {
            if ("ko".equalsIgnoreCase(targetLang)) {
                return translateJaToKo(text).getTranslatedText();

            } else if ("jp".equalsIgnoreCase(targetLang)) {
                return translateKoToJa(text).getTranslatedText();

            } else {
                return text;
            }

        } catch (Exception e) {
            log.error("TranslateText error: {}", e.getMessage());
            return text;
        }
    }
}
