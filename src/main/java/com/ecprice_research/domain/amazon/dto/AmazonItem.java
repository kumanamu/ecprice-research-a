package com.ecprice_research.domain.amazon.dto;

import lombok.Data;

@Data
public class AmazonItem {

    private String title;
    private String link;
    private String image;
    private long price; // JPY

    public AmazonItem(String title, String link, String image, long price) {
        this.title = title;
        this.link = link;
        this.image = image;
        this.price = price;
    }
}
