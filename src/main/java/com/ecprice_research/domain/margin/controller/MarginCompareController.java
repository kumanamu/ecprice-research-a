package com.ecprice_research.domain.margin.controller;

import com.ecprice_research.domain.margin.dto.MarginRequest;
import com.ecprice_research.domain.margin.dto.MarginResponse;
import com.ecprice_research.domain.margin.service.TotalMarginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/margin")
public class MarginCompareController {

    private final TotalMarginService totalService;

    @GetMapping("/compare")
    public MarginResponse compare(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "kr") String lang
    ) {
        MarginRequest req = new MarginRequest();
        req.setKeyword(keyword);
        req.setLang(lang);

        return totalService.compare(req);
    }
}

