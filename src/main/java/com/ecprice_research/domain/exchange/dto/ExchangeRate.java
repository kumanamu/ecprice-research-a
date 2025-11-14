package com.ecprice_research.domain.exchange.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExchangeRate {
    private long jpyToKrw;
    private double krwToJpy;
}
