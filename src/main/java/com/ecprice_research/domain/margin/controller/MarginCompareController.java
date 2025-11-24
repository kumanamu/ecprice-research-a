package com.ecprice_research.domain.margin.controller;

import com.ecprice_research.domain.margin.dto.MarginCompareResult;
import com.ecprice_research.domain.margin.service.MarginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/margin")
@RequiredArgsConstructor
public class MarginCompareController {

    private final MarginService marginService;

    @GetMapping("/compare")
    public MarginCompareResult compare(
            @RequestParam String keyword,
            @RequestParam String lang,
            @RequestParam(defaultValue = "false") boolean premium

    ) {

        log.info("🔥 [Controller] keyword='{}' lang='{}' premium={}", keyword, lang, premium);
        return marginService.compare(keyword, lang, premium);
    }
}
