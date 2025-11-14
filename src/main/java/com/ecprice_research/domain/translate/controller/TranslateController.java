package com.ecprice_research.domain.translate.controller;

import com.ecprice_research.domain.translate.dto.TranslateRequest;
import com.ecprice_research.domain.translate.dto.TranslateResponse;
import com.ecprice_research.domain.translate.service.TranslateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/translate")
@RequiredArgsConstructor
public class TranslateController {

    private final TranslateService translateService;

    @PostMapping("/ko-to-ja")
    public TranslateResponse koToJa(@RequestBody TranslateRequest req) {
        return translateService.translateKoToJa(req.getText());
    }

    @PostMapping("/ja-to-ko")
    public TranslateResponse jaToKo(@RequestBody TranslateRequest req) {
        return translateService.translateJaToKo(req.getText());
    }
}
