package com.ecprice_research.domain.coupang.dto;

import lombok.Data;

@Data
public class CoupangItem {

    private String title;
    private String link;
    private String image;
    private long price; // KRW

    public CoupangItem(String title, String link, String image, long price) {
        this.title = title;
        this.link = link;
        this.image = image;
        this.price = price;
    }
}
