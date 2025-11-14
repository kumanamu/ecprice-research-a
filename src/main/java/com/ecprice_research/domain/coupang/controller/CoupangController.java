package com.ecprice_research.domain.coupang.controller;

import com.ecprice_research.domain.coupang.service.CoupangService;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupang")
@RequiredArgsConstructor
public class CoupangController {

    private final CoupangService coupangService;

    @GetMapping("/search")
    public PriceInfo search(@RequestParam String keyword) {
        return coupangService.search(keyword);
    }
}
