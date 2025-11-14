package com.ecprice_research.domain.rakuten.dto;

import lombok.Data;

@Data
public class RakutenItem {
    private String title;
    private String url;
    private String image;
    private long price; // JPY 라쿠텐 기본 통화

    public RakutenItem(String title, String url, String image, long price) {
        this.title = title;
        this.url = url;
        this.image = image;
        this.price = price;
    }
}
