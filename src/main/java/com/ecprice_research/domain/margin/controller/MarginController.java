package com.ecprice_research.domain.margin.controller;

import com.ecprice_research.domain.margin.service.MarginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MarginController {

    private final MarginService marginService;

    @GetMapping("/margin")
    public ResponseEntity<?> compare(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "ko") String lang
    ) {

        log.info("🔥 [Controller] GET /api/margin keyword='{}', lang='{}'", keyword, lang);

        // marginService.compare() 내부에서 basic + premium 모두 생성
        return ResponseEntity.ok(marginService.compare(keyword, lang));
    }
}
