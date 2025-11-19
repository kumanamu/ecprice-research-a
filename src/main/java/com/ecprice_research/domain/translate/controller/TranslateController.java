package com.ecprice_research.domain.translate.controller;

import com.ecprice_research.domain.translate.dto.TranslateRequest;
import com.ecprice_research.domain.translate.dto.TranslateResponse;
import lombok.RequiredArgsConstructor;
import com.ecprice_research.domain.translate.service.TranslateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/translate")
@RequiredArgsConstructor
public class TranslateController {

    private final TranslateService translateService;

    @PostMapping
    public TranslateResponse translate(@RequestBody TranslateRequest req) {

        String text = req.getText();
        String source = req.getSource();
        String target = req.getTarget();

        log.info("📝 번역 요청: {} → {}", source, target);

        // ko → ja
        if ("ko".equals(source) && "ja".equals(target)) {
            String translated = translateService.koToJp(text);
            return TranslateResponse.builder()
                    .originalText(text)
                    .translatedText(translated)
                    .sourceLang("ko")
                    .targetLang("ja")
                    .build();
        }

        // ja → ko
        if ("ja".equals(source) && "ko".equals(target)) {
            String translated = translateService.jpToKo(text); // 🔥 형 코드에 딱 맞는 메소드
            return TranslateResponse.builder()
                    .originalText(text)
                    .translatedText(translated)
                    .sourceLang("ja")
                    .targetLang("ko")
                    .build();
        }

        // 기타 조합 — 원문 그대로
        return TranslateResponse.builder()
                .originalText(text)
                .translatedText(text)
                .sourceLang(source)
                .targetLang(target)
                .build();
    }


    @GetMapping("/auto")
    public TranslateResponse autoTranslate(
            @RequestParam String text,
            @RequestParam String lang
    ) {
        log.info("🌍 auto-translate 요청: lang={}, text={}", lang, text);

        if ("jp".equals(lang)) { // 한국어를 일본어로
            String translated = translateService.koToJp(text);
            return TranslateResponse.builder()
                    .originalText(text)
                    .translatedText(translated)
                    .sourceLang("ko")
                    .targetLang("ja")
                    .build();
        }

        if ("ko".equals(lang)) { // 일본어를 한국어로
            String translated = translateService.jpToKo(text);
            return TranslateResponse.builder()
                    .originalText(text)
                    .translatedText(translated)
                    .sourceLang("ja")
                    .targetLang("ko")
                    .build();
        }

        return TranslateResponse.builder()
                .originalText(text)
                .translatedText(text)
                .sourceLang(lang)
                .targetLang(lang)
                .build();
    }
}
