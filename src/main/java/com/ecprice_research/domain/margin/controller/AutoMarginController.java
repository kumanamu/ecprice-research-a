package com.ecprice_research.domain.margin.controller;

import com.ecprice_research.domain.margin.dto.AiMarginAnalysis;
import com.ecprice_research.domain.margin.dto.MarginCompareResult;
import com.ecprice_research.domain.margin.service.OpenAiAnalysisService;
import com.ecprice_research.domain.margin.service.MarginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/margin")
@RequiredArgsConstructor
public class AutoMarginController {

    private final MarginService marginService;
    private final OpenAiAnalysisService openAiService;

    @GetMapping("/auto")
    public AiMarginAnalysis autoMargin(
            @RequestParam String keyword,
            @RequestParam String lang,
            @RequestParam(defaultValue = "false") boolean premium
    ) {
        MarginCompareResult result = marginService.compare(keyword, lang, premium);
        return openAiService.analyze(result, premium);
    }
}
