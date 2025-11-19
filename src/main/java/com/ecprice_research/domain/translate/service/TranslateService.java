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
 * 번역 서비스
 * - OpenAI API 호출 전 캐싱을 먼저 조회하여 비용 및 지연을 줄이는 구조
 * - 운영환경에서는 반드시 필요한 성능 최적화 포인트
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TranslateService {

    @Value("${openai.key}")
    private String OPENAI_KEY;

    private final RestTemplate restTemplate = new RestTemplate();

    // ---------------------------------------------------------------------
    // 🔥 안전 번역기 (어떤 언어든 → 원하는 언어로)
    // ---------------------------------------------------------------------
    public String safeTranslate(String text, String from, String to) {

        String cacheKey = "SAFE_" + from + "_" + to + "_" + text;

        // 캐시 확인
        String cached = TranslateCache.getKoToJp(cacheKey);
        if (cached != null) return cached;

        try {
            String prompt = """
                Translate the following text precisely.
                From: %s
                To: %s
                Text: %s
            """.formatted(from, to, text);

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

            List choices = (List) res.get("choices");
            Map first = (Map) choices.get(0);
            Map message = (Map) first.get("message");
            String translated = (String) message.get("content");

            // 캐싱 저장
            TranslateCache.putKoToJp(cacheKey, translated);

            return translated;

        } catch (Exception e) {
            log.error("❌ safeTranslate 실패: {}", e.getMessage());
            return text; // 실패하면 원문 유지
        }
    }


    // --------------------------------------------------------------
    // 단일언어 번역 (캐싱 있는 버전)
    // --------------------------------------------------------------
    public String koToJp(String text) {
        String cached = TranslateCache.getKoToJp(text);
        if (cached != null) return cached;

        String result = safeTranslate(text, "ko", "jp");
        TranslateCache.putKoToJp(text, result);
        return result;
    }

    public String jpToKo(String text) {
        String cached = TranslateCache.getJpToKo(text);
        if (cached != null) return cached;

        String result = safeTranslate(text, "jp", "ko");
        TranslateCache.putJpToKo(text, result);
        return result;
    }
}
