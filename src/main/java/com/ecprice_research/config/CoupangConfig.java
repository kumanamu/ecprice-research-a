package com.ecprice_research.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "coupang")
public class CoupangConfig {

    private String accessKey;
    private String secretKey;

    public String buildSearchPath(String keyword) {
        return "/v2/providers/affiliate_open_api/apis/openapi/v1/products/search?keyword=" + keyword;
    }
}
