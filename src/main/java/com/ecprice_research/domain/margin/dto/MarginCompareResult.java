package com.ecprice_research.domain.margin.dto;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MarginCompareResult {

    private String keyword;

    private PriceInfo amazonJp;
    private PriceInfo rakuten;
    private PriceInfo naver;
    private PriceInfo coupang;

    private long jpyToKrw;
    private double krwToJpy;

    private String bestPlatform;
    private long profitKrw;
    private double profitJpy;
}
