package com.ecprice_research.domain.margin.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PriceInfo {

    private String platform;           // AMAZON_JP, RAKUTEN, NAVER, COUPANG

    private String productName;
    private String productUrl;
    private String productImage;

    private long priceOriginal;        // 원본 가격 (JPY or KRW)
    private long shippingOriginal;     // 원본 배송비
    private String currencyOriginal;   // "JPY" or "KRW"

    private long priceKrw;             // KRW 기준가격
    private long priceJpy;             // JPY 기준가격

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now(); // 수집 시각
}

