package com.ecprice_research.domain.engine;

import com.ecprice_research.domain.translate.service.TranslateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * C-엔진 핵심: 검색어 감지 → 변환 → 플랫폼별 키워드 제공
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordConvert {

    private final TranslateService translate;

    /**
     * 감지 결과: KR / JP / EN
     */
    public String detectLang(String text) {

        if (text.matches(".*[가-힣].*")) return "KR";
        if (text.matches(".*[ぁ-んァ-ン一-龥].*")) return "JP";
        return "EN";
    }

    /**
     * 검색 키워드 최종 변환
     */
    public SearchKeywords convert(String keyword, String detected) {

        String jp;
        String kr;

        switch (detected) {
            case "KR" -> {
                jp = translate.koToJp(keyword);
                kr = keyword;
            }
            case "JP" -> {
                jp = keyword;
                kr = translate.jpToKo(keyword);
            }
            default -> {
                // 영어는 원문 그대로
                jp = keyword;
                kr = keyword;
            }
        }

        log.info("🔤 [C-Engine] detected={}, jp='{}', kr='{}'", detected, jp, kr);

        return new SearchKeywords(
                jp,  // Amazon
                jp,  // Rakuten
                kr,  // Naver
                kr   // Coupang
        );
    }

    /**
     * 플랫폼별 키워드 묶음
     */
    public record SearchKeywords(
            String amazon,
            String rakuten,
            String naver,
            String coupang
    ) {}
}
