package com.ecprice_research.domain.naver.controller;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.search.engine.CEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/naver")
@RequiredArgsConstructor
public class NaverController {

    private final CEngine cEngine;

    @GetMapping("/search")
    public PriceInfo search(@RequestParam String keyword) {
        return cEngine.runSingle("NAVER_SHOPPING", keyword);
    }
}
