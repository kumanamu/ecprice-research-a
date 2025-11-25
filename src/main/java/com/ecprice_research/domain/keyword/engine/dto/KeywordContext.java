package com.ecprice_research.domain.keyword.engine.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 엔진이 받아들이는 입력값 + 현재 처리 단계의 컨텍스트
 */
@Getter
@Builder
public class KeywordContext {

    /** 🔑 원본 입력값 */
    private final String originalKeyword;

    /** 🔥 언어 토글 (ko / jp) - 출력 언어 결정 */
    private final String toggleLang;

    /** 🔍 감지된 언어 (KR / JP / EN) */
    private final String detectedLang;

    /** 🧠 엔진 모드 (DETECT / CONVERT / VARIANT / PIPELINE) */
    private final EngineMode mode;

    /** ⚙️ 중간 결과 저장용 (JP 변환 문자열) */
    private final String jpKeyword;

    /** ⚙️ 중간 결과 저장용 (KR 변환 문자열) */
    private final String krKeyword;
}
