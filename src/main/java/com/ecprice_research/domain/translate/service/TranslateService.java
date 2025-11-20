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

/**
 * 번역 서비스 - 지침 100% 반영
 * 영어는 절대 번역하지 않음
 * 일본 사이트는 일본어로 / 한국 사이트는 한국어로 검색
 * 출력 번역은 MarginService에서 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TranslateService {

    @Value("${OPENAI_API_KEY}")
    private String OPENAI_KEY;

    private final RestTemplate restTemplate = new RestTemplate();

    // ======================================================================
    // 🔥 공통 안전 번역 API
    // ======================================================================
    private String callOpenAi(String prompt) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + OPENAI_KEY);
            headers.set("Content-Type", "application/json");

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
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


    // ======================================================================
    // 🔥 영어 → 번역 금지 규칙
    // ======================================================================
    private boolean isEnglishOnly(String text) {
        return text.matches("^[a-zA-Z0-9\\s\\-_.]+$");
    }

    private boolean isKorean(String text) {
        return text.matches(".*[가-힣].*");
    }

    private boolean isJapanese(String text) {
        return text.matches(".*[一-龯ぁ-ゔァ-ヴー々〆〤].*");
    }


    // ======================================================================
    // 🔥 ko → jp
    // ======================================================================
    public String koToJp(String text) {

        if (text == null || text.isBlank()) return text;
        if (isEnglishOnly(text)) return text;  // 영어 → 그대로

        String cached = TranslateCache.get("KO_JP_" + text);
        if (cached != null) return cached;

        String prompt = """
            Translate this text from Korean to Japanese.
            Output ONLY the translation.
            Text: %s
        """.formatted(text);

        String result = callOpenAi(prompt);
        if (result == null) result = text;

        TranslateCache.put("KO_JP_" + text, result);
        return result;
    }


    // ======================================================================
    // 🔥 jp → ko
    // ======================================================================
    public String jpToKo(String text) {

        if (text == null || text.isBlank()) return text;
        if (isEnglishOnly(text)) return text; // 영어 → 그대로

        String cached = TranslateCache.get("JP_KO_" + text);
        if (cached != null) return cached;

        String prompt = """
            Translate this text from Japanese to Korean.
            Output ONLY the translation.
            Text: %s
        """.formatted(text);

        String result = callOpenAi(prompt);
        if (result == null) result = text;

        TranslateCache.put("JP_KO_" + text, result);
        return result;
    }


    // ======================================================================
    // 🔥 ko → en
    // ======================================================================
    public String koToEn(String text) {

        if (text == null || text.isBlank()) return text;
        if (isEnglishOnly(text)) return text; // 영어는 번역 금지

        String cached = TranslateCache.get("KO_EN_" + text);
        if (cached != null) return cached;

        String prompt = """
            Translate this text from Korean to English.
            Output ONLY the translation.
            Text: %s
        """.formatted(text);

        String result = callOpenAi(prompt);
        if (result == null) result = text;

        TranslateCache.put("KO_EN_" + text, result);
        return result;
    }


    // ======================================================================
    // 🔥 jp → en
    // ======================================================================
    public String jpToEn(String text) {

        if (text == null || text.isBlank()) return text;
        if (isEnglishOnly(text)) return text; // 영어는 번역 금지

        String cached = TranslateCache.get("JP_EN_" + text);
        if (cached != null) return cached;

        String prompt = """
            Translate this text from Japanese to English.
            Output ONLY the translation.
            Text: %s
        """.formatted(text);

        String result = callOpenAi(prompt);
        if (result == null) result = text;

        TranslateCache.put("JP_EN_" + text, result);
        return result;
    }
}
