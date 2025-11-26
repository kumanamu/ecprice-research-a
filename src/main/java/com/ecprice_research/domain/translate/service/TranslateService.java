package com.ecprice_research.domain.translate.service;

import com.ecprice_research.domain.openai.OpenAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslateService {

    private final OpenAiClient ai;

    /**
     * 🇰🇷 한국어 → 🇯🇵 일본어 번역
     */
    public String koToJp(String text) {
        try {
            String prompt = """
                    다음 한국어 문장을 자연스러운 일본어로 번역하세요.
                    번역만 출력하세요.

                    한국어: %s
                    """.formatted(text);

            String res = ai.ask(prompt);
            return clean(res);

        } catch (Exception e) {
            log.error("❌ koToJp 번역 실패: {}", e.getMessage());
            return text;
        }
    }

    /**
     * 🇯🇵 일본어 → 🇰🇷 한국어 번역
     */
    public String jpToKo(String text) {
        try {
            String prompt = """
                    다음 일본어 문장을 자연스러운 한국어로 번역하세요.
                    번역만 출력하세요.

                    일본어: %s
                    """.formatted(text);

            String res = ai.ask(prompt);
            return clean(res);

        } catch (Exception e) {
            log.error("❌ jpToKo 번역 실패: {}", e.getMessage());
            return text;
        }
    }

    /**
     * GPT 응답에서 공백/따옴표 제거
     */
    private String clean(String s) {
        if (s == null) return "";
        return s.trim()
                .replace("\"", "")
                .replace("\n", "")
                .replace("\r", "");
    }
}
