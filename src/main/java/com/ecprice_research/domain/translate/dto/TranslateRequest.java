package com.ecprice_research.domain.translate.dto;

import lombok.Data;

@Data
public class TranslateRequest {
    private String text;
    private String source;
    private String target;
}
