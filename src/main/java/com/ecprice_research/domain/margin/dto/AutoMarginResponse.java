package com.ecprice_research.domain.margin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AutoMarginResponse {

    private long autoBuyPrice;
    private long autoSellPrice;
    private long autoProfit;
    private double autoProfitRate;

    private String aiStrategy;
}
