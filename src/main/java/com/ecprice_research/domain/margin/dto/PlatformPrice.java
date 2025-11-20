package com.ecprice_research.domain.margin.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformPrice {
    private String productName;
    private String productUrl;
    private String productImage;

    private double priceKrw;
    private double priceJpy;
}
