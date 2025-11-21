package com.ecprice_research.domain.margin.controller;

import com.ecprice_research.domain.margin.dto.MarginCompareResult;
import com.ecprice_research.domain.margin.service.MarginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/margin")
@RequiredArgsConstructor
public class PremiumMarginController {

    private final MarginService marginService;

    @GetMapping("/premium")
    public MarginCompareResult comparePremium(
            @RequestParam String keyword,
            @RequestParam String lang
    ) {
        return marginService.compare(keyword, lang, true);
    }
}
