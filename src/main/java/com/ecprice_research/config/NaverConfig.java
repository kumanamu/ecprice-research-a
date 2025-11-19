package com.ecprice_research.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "naver.client")
public class NaverConfig {

    private String id;
    private String secret;

    public String buildSearchUrl(String keyword) {
        return "https://openapi.naver.com/v1/search/shop.json?query=" + keyword;
    }
}
