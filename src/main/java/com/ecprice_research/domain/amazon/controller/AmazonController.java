package com.ecprice_research.domain.amazon.controller;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.search.engine.CEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/amazon")
@RequiredArgsConstructor
public class AmazonController {

    private final CEngine cEngine;

    @GetMapping("/search")
    public PriceInfo search(@RequestParam String keyword) {
        return cEngine.runSingle("AMAZON_JP", keyword);
    }
}
