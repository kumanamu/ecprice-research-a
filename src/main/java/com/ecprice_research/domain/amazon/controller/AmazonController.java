package com.ecprice_research.domain.amazon.controller;

import com.ecprice_research.domain.amazon.service.AmazonService;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/amazon")
@RequiredArgsConstructor
public class AmazonController {

    private final AmazonService amazonService;

    @GetMapping("/search")
    public PriceInfo search(@RequestParam String keyword) {
        return (PriceInfo) amazonService.search(keyword);
    }
}
