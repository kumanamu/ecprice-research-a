package com.ecprice_research.domain.keyword.engine.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

/**
 * 엔진 최종 결과 DTO
 * - JP/Amazon 용
 * - Rakuten 용
 * - Naver 용
 * - Coupang 용
 * - 감지언어 / 토글 / 원본 정보를 함께 리턴
 */
@Getter
@Builder
public class KeywordResult {

    /** 🔑 원본 입력값 */
    private final String originalKeyword;

    /** 🔥 출력 토글 (ko / jp) */
    private final String toggleLang;

    /** 🔍 감지된 입력 언어 (KR / JP / EN) */
    private final String detectedLang;

    /** 🧠 엔진 최종 변환 (JP/KR 중간 결과 포함) */
    private final String jpKeyword;
    private final String krKeyword;

    /** 📦 플랫폼별 최종 검색어 리스트 */
    private final List<String> amazonKeywords;
    private final List<String> rakutenKeywords;
    private final List<String> naverKeywords;
    private final List<String> coupangKeywords;
}
