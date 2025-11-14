package com.ecprice_research.domain.rakuten.controller;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import com.ecprice_research.domain.rakuten.service.RakutenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rakuten")
@RequiredArgsConstructor
public class RakutenController {

    private final RakutenService rakutenService;

    @GetMapping("/search")
    public PriceInfo search(@RequestParam String keyword) {
        return rakutenService.search(keyword);
    }
}
