package com.ecprice_research.domain.keyword.engine.dto;

public enum EngineMode {

    /**
     * 1) 언어 감지 단계
     * - KR / JP / EN 판별
     */
    DETECT,

    /**
     * 2) 변환 단계
     * - KR → JP
     * - JP → KR
     * - EN → RAW (절대 번역 금지)
     */
    CONVERT,

    /**
     * 3) 후보 생성 단계
     * - Amazon/Rakuten: JP
     * - Naver/Coupang: KR
     * - variants 키워드 빌드
     */
    VARIANT,

    /**
     * 전체 파이프라인 실행
     * detect → convert → variant
     */
    PIPELINE;
}
