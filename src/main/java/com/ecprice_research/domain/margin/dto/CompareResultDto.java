package com.ecprice_research.domain.margin.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompareResultDto {
    private PlatformPrice amazonJp;
    private PlatformPrice rakuten;
    private PlatformPrice naver;
    private PlatformPrice coupang;

    private String bestPlatform;  // 최저가 사업자
    private double jpyToKrw;      // 환율
    private double profitKrw;     // 자동 마진 계산
    private double profitJpy;
}
