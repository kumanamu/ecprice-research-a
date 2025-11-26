package com.ecprice_research.domain.coupang.controller;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.search.engine.CEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupang")
@RequiredArgsConstructor
public class CoupangController {

    private final CEngine cEngine;

    @GetMapping("/search")
    public PriceInfo search(@RequestParam String keyword) {
        return cEngine.runSingle("COUPANG", keyword);
    }
}
