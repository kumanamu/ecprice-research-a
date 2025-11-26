package com.ecprice_research.domain.margin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarginRequest {

    private String keyword;     // 검색어
    private String lang;        // 출력 언어 (kr/jp)
    private boolean premium;    // 프리미엄 분석 여부

    private double krwRate;     // 1 JPY → KRW 환율 (예: 9.5)
    private double jpyRate;     // 1 KRW → JPY 환율 (예: 0.11)

    // 필요하면 배송비 / 기타 옵션 추가 가능
}
