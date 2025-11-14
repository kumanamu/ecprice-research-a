package com.ecprice_research.domain.naver.dto;

import lombok.Data;

@Data
public class NaverItem {

    private String title;
    private String link;
    private String image;
    private long price; // 네이버는 KRW 그대로

    public NaverItem(String title, String link, String image, long price) {
        this.title = title;
        this.link = link;
        this.image = image;
        this.price = price;
    }
}
