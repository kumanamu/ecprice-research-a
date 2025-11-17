package com.ecprice_research.domain.margin.controller;

import com.ecprice_research.domain.margin.dto.MarginCompareResult;
import com.ecprice_research.domain.margin.service.MarginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/margin")
@RequiredArgsConstructor
public class MarginController {

    private final MarginService marginService;

    @GetMapping("/compare")
    public MarginCompareResult compare(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "ko") String lang
    ) {
        return marginService.compare(keyword, lang);
    }
}
