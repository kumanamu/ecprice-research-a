package com.ecprice_research.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "rakuten.api")
public class RakutenConfig {

    private String key;
    private String affiliate;
    private String apiUrl;

    public String buildSearchUrl(String keyword) {
        return apiUrl
                + "?applicationId=" + key
                + "&affiliateId=" + affiliate
                + "&keyword=" + keyword
                + "&format=json";
    }
}
