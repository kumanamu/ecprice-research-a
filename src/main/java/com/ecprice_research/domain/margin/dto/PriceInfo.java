package com.ecprice_research.domain.margin.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PriceInfo {

    private String platform;
    private String productName;
    private String productUrl;
    private String productImage;

    private Integer priceOriginal;   // null 허용
    private Integer shippingOriginal;

    private String currencyOriginal;

    private Integer priceKrw;
    private Integer priceJpy;
    private String displayPrice;

    private String status; // SUCCESS / NOT_FOUND
    private String reason;

    private String country;
    private java.time.LocalDateTime timestamp;

    public static PriceInfo notFound(String platform, String reason) {
        return PriceInfo.builder()
                .platform(platform)
                .status("EMPTY")
                .productName("검색 결과 없음")
                .productUrl("")
                .productImage("")
                .priceOriginal(0)
                .currencyOriginal("JPY")
                .priceJpy(0)
                .priceKrw(0)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    public static PriceInfo empty(String platform) {
        return PriceInfo.builder()
                .platform(platform)
                .status("EMPTY")
                .productName("검색 결과 없음")
                .productUrl("")
                .productImage("")
                .priceOriginal(0)
                .shippingOriginal(0)
                .currencyOriginal("JPY")
                .priceKrw(0)
                .priceJpy(0)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    public boolean isSuccess() {
        return "SUCCESS".equalsIgnoreCase(this.status);
    }
}
