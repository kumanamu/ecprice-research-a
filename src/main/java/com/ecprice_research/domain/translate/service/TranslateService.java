package com.ecprice_research.domain.translate.service;

import com.ecprice_research.domain.keyword.engine.UnifiedCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * 🔥 C-엔진 통합 번역 서비스 (최종 안정판)
 * - UnifiedCache 사용
 * - 번역 규칙 헌법 100% 준수
 *   1) 영어-only → 절대 번역 금지
 *   2) 영어 포함 혼합 → 절대 번역 금지
 *   3) 한국어 → 일본어
 *   4) 일본어 → 한국어
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TranslateService {

    @Value("${OPENAI_API_KEY}")
    private String OPENAI_KEY;

    private final UnifiedCache unifiedCache;
    private final RestTemplate restTemplate = new RestTemplate();

    // ============================================================
    // 🔥 OpenAI 요청 공통부
    // ============================================================
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

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            Map res = restTemplate.postForObject(
                    "https://api.openai.com/v1/chat/completions",
                    entity,
                    Map.class
            );

            if (res == null) return null;

            List choices = (List) res.get("choices");
            if (choices == null || choices.isEmpty()) return null;

            Map first = (Map) choices.get(0);
            Map msg = (Map) first.get("message");

            return (String) msg.getOrDefault("content", null);

        } catch (Exception e) {
            log.error("❌ OpenAI 번역 실패: {}", e.getMessage());
            return null;
        }
    }

    // ============================================================
    // 🔍 언어 감지
    // ============================================================
    private boolean isEnglishOnly(String text) {
        return text.matches("^[a-zA-Z0-9\\s\\-_.]+$");
    }

    private boolean hasEnglish(String text) {
        return text.matches(".*[a-zA-Z].*");
    }

    private boolean isKorean(String text) {
        return text.matches(".*[가-힣].*");
    }

    private boolean isJapanese(String text) {
        return text.matches(".*[一-龥ぁ-ゔァ-ヴー々〆〤].*");
    }

    private boolean isMixed(String text) {
        int c = 0;
        if (isKorean(text)) c++;
        if (isJapanese(text)) c++;
        if (hasEnglish(text)) c++;
        return c >= 2;
    }

    // ============================================================
    // 🔐 캐시 + 번역 공통 처리
    // ============================================================
    private String cachedTranslate(String key, String prompt, String fallback) {

        List<String> cache = unifiedCache.getList(key);
        if (cache != null && !cache.isEmpty()) {
            log.info("💾 [번역 캐시 HIT] {} → {}", key, cache.get(0));
            return cache.get(0);
        }

        log.info("🌐 [OpenAI 번역 요청] {}", key);
        String res = callOpenAi(prompt);
        if (res == null || res.isBlank()) res = fallback;

        unifiedCache.put(key, List.of(res));
        log.info("💾 [번역 캐시 저장] {} → {}", key, res);

        return res;
    }

    // ============================================================
    // 🔥 한국어 → 일본어
    // ============================================================
    public String koToJp(String text) {

        if (text == null || text.isBlank()) return text;

        // 헌법 1조: 영어는 무조건 번역 금지
        if (isEnglishOnly(text) || hasEnglish(text)) {
            log.info("🔒 [영어 입력 → 번역 스킵] {}", text);
            return text;
        }

        // 혼합 입력 또한 번역 금지
        if (isMixed(text)) {
            log.info("🔒 [혼합 입력 → 번역 스킵] {}", text);
            return text;
        }

        String key = "KO_JP_" + text;

        String prompt = """
            Translate from Korean to Japanese.
            Output ONLY the translation.
            Text: %s
        """.formatted(text);

        return cachedTranslate(key, prompt, text);
    }

    // ============================================================
    // 🔥 일본어 → 한국어
    // ============================================================
    public String jpToKo(String text) {

        if (text == null || text.isBlank()) return text;

        if (isEnglishOnly(text) || hasEnglish(text)) {
            log.info("🔒 [영어 입력 → 번역 스킵] {}", text);
            return text;
        }

        if (isMixed(text)) {
            log.info("🔒 [혼합 입력 → 번역 스킵] {}", text);
            return text;
        }

        String key = "JP_KO_" + text;

        String prompt = """
            Translate from Japanese to Korean.
            Output ONLY the translation.
            Text: %s
        """.formatted(text);

        return cachedTranslate(key, prompt, text);
    }

    // ============================================================
    // 🔥 한국어 → 영어 (선택)
    // ============================================================
    public String koToEn(String text) {

        if (text == null || text.isBlank()) return text;

        if (isEnglishOnly(text)) return text;
        if (hasEnglish(text)) return text;
        if (isMixed(text)) return text;

        String key = "KO_EN_" + text;

        String prompt = """
            Translate Korean to English.
            Output ONLY the translation.
            Text: %s
        """.formatted(text);

        return cachedTranslate(key, prompt, text);
    }

    // ============================================================
    // 🔥 일본어 → 영어 (선택)
    // ============================================================
    public String jpToEn(String text) {

        if (text == null || text.isBlank()) return text;

        if (isEnglishOnly(text)) return text;
        if (hasEnglish(text)) return text;
        if (isMixed(text)) return text;

        String key = "JP_EN_" + text;

        String prompt = """
            Translate Japanese to English.
            Output ONLY the translation.
            Text: %s
        """.formatted(text);

        return cachedTranslate(key, prompt, text);
    }
}
