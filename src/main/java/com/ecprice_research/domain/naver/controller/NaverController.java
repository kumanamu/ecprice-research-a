package com.ecprice_research.domain.naver.controller;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.naver.service.NaverService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/naver")
@RequiredArgsConstructor
public class NaverController {

    private final NaverService naverService;

    @GetMapping("/search")
    public PriceInfo search(@RequestParam String keyword) {
        return (PriceInfo) naverService.search(keyword);
    }
}
