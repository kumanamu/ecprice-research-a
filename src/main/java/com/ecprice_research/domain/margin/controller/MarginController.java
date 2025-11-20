package com.ecprice_research.domain.margin.controller;

import com.ecprice_research.domain.margin.dto.MarginCompareResult;
import com.ecprice_research.domain.margin.dto.MarginSimulationRequest;
import com.ecprice_research.domain.margin.dto.MarginSimulationResult;
import com.ecprice_research.domain.margin.service.MarginService;
import com.ecprice_research.domain.margin.service.MarginSimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/margin")
@RequiredArgsConstructor
public class MarginController {

    private final MarginService marginService;
    private final MarginSimulationService simService;


    // ============================================================
    // 🔥 마진 시뮬레이션 API
    // ============================================================
    @PostMapping("/simulation")
    public MarginSimulationResult simulation(@RequestBody MarginSimulationRequest req) {
        return simService.simulate(req);
    }
}
