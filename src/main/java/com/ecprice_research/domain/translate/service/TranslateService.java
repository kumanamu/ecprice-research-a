package com.ecprice_research.domain.translate.service;

import com.ecprice_research.util.TranslateCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslateService {

    @Value("${openai.api.key}")
    private String OPENAI_KEY;

    @Value("${openai.api.model}") // gpt-4o-mini 사용
    private String OPENAI_MODEL;

    private final RestTemplate restTemplate = new RestTemplate();

    // --------------------------------------------------------------------
    // 공통 OpenAI 호출
    // --------------------------------------------------------------------
    private String callOpenAi(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + OPENAI_KEY);
            headers.set("Content-Type", "application/json");

            Map<String, Object> body = Map.of(
                    "model", OPENAI_MODEL,
                    "temperature", 0.2,
                    "max_tokens", 200,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    )
            );

            HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

            Map res = restTemplate.postForObject(
                    "https://api.openai.com/v1/chat/completions",
                    req,
                    Map.class
            );

            if (res == null) return null;

            List choices = (List) res.get("choices");
            if (choices == null || choices.isEmpty()) return null;

            Map first = (Map) choices.get(0);
            Map message = (Map) first.get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            log.error("❌ 번역 실패: {}", e.getMessage());
            return null;
        }
    }

    private boolean isEnglishOnly(String text) {
        return text.matches("^[a-zA-Z0-9\\s\\-_.]+$");
    }

    private boolean isKorean(String text) {
        return text.matches(".*[가-힣].*");
    }

    private boolean isJapanese(String text) {
        return text.matches(".*[一-龯ぁ-ゔァ-ヴー々〆〤].*");
    }

    // --------------------------------------------------------------------
    // ko → jp
    // --------------------------------------------------------------------
    public String koToJp(String text) {
        if (text == null || text.isBlank()) return text;
        if (isEnglishOnly(text)) return text;

        String key = "KO_JP_" + text;
        String cached = TranslateCache.get(key);
        if (cached != null) return cached;

        String prompt = """
            Translate this Korean text to Japanese. 
            Output ONLY the translation:
            %s
        """.formatted(text);

        String result = callOpenAi(prompt);
        if (result == null) result = text;

        TranslateCache.put(key, result);
        return result;
    }

    // --------------------------------------------------------------------
    // jp → ko
    // --------------------------------------------------------------------
    public String jpToKo(String text) {
        if (text == null || text.isBlank()) return text;
        if (isEnglishOnly(text)) return text;

        String key = "JP_KO_" + text;
        String cached = TranslateCache.get(key);
        if (cached != null) return cached;

        String prompt = """
            Translate this Japanese text to Korean. 
            Output ONLY the translation:
            %s
        """.formatted(text);

        String result = callOpenAi(prompt);
        if (result == null) result = text;

        TranslateCache.put(key, result);
        return result;
    }
}
