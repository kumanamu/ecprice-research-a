package com.ecprice_research.domain.translate.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TranslateResponse {
    private String originalText;  // 입력 문장
    private String translatedText; // 번역 결과
    private String sourceLang;    // 감지된 언어 (optional)
    private String targetLang;
}
