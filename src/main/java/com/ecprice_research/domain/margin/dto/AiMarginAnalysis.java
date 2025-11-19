package com.ecprice_research.domain.margin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiMarginAnalysis {

    private String buyPlatform;
    private String sellPlatform;
    private long profitKrw;
    private double profitRate;
    private String reason;

    public String summary() {
        return "Buy: " + buyPlatform
                + "\nSell: " + sellPlatform
                + "\nProfit: " + profitKrw + " KRW (" + profitRate + "%)"
                + "\nReason: " + reason;
    }
}
