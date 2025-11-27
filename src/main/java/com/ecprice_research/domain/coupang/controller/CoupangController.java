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

    /**
     * 🔥 쿠팡 검색 (단일 PriceInfo 반환)
     * - 기존 List<PriceInfo> → PriceInfo 로 구조統一
     */
    @GetMapping("/search")
    public PriceInfo search(@RequestParam String keyword) {
        return coupangService.search(keyword);
    }
}
