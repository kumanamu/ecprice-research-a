package com.ecprice_research.keyword.engine;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KeywordDetect {

    public enum LangType {
        KR, JP, EN, MIXED, UNKNOWN
    }

    /**
     * 🔍 입력 문자열의 언어 감지
     * - 한글 → KR
     * - 일본어(한자/히라가나/가타카나) → JP
     * - 영어/숫자/영문기호 → EN
     * - 복합 → MIXED
     */
    public static LangType detect(String text) {

        if (text == null || text.isBlank()) return LangType.UNKNOWN;

        boolean hasKr = text.matches(".*[가-힣].*");
        boolean hasJp = text.matches(".*[ぁ-んァ-ン一-龥々〆〤].*");
        boolean hasEn = text.matches(".*[a-zA-Z].*");

        int count = (hasKr ? 1 : 0) + (hasJp ? 1 : 0) + (hasEn ? 1 : 0);

        // 언어 2개 이상 섞이면 → MIXED
        if (count > 1) return LangType.MIXED;

        if (hasKr) return LangType.KR;
        if (hasJp) return LangType.JP;
        if (hasEn) return LangType.EN;

        // 전부 아니면 그냥 UNKNOWN
        return LangType.UNKNOWN;
    }

    /**
     * 🔥 영어-only 여부 (번역 금지 규칙에 사용)
     */
    public static boolean isEnglishOnly(String text) {
        return text != null &&
                text.matches("^[a-zA-Z0-9\\s\\-_.]+$");
    }
}
