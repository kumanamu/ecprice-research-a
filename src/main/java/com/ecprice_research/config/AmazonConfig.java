package com.ecprice_research.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "serpapi.api")
public class AmazonConfig {

    private String key;  // SERPAPI_API_KEY

    public String buildSearchUrl(String keyword) {
        return "https://serpapi.com/search.json"
                + "?engine=amazon"
                + "&amazon_domain=amazon.co.jp"
                + "&gl=jp"
                + "&hl=ja"
                + "&k=" + keyword
                + "&api_key=" + key;
    }
}
