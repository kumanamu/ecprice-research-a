package com.ecprice_research.domain.margin.controller;

import com.ecprice_research.domain.margin.service.CompareService;
import com.ecprice_research.domain.margin.service.AutoMarginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/margin")
@RequiredArgsConstructor
public class MarginCompareController {

    private final CompareService compareService;
    private final AutoMarginService autoMarginService;

    @GetMapping("/compare")
    public Object compare(@RequestParam String keyword,
                          @RequestParam(defaultValue = "ko") String lang) {

        var raw = compareService.compare(keyword);
        var full = autoMarginService.attachAutoMargin(raw);

        return full;
    }
}
