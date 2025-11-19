package com.ecprice_research.domain.margin.controller;

import com.ecprice_research.domain.margin.dto.MarginSimulationRequest;
import com.ecprice_research.domain.margin.dto.MarginSimulationResult;
import com.ecprice_research.domain.margin.service.MarginSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/margin/sim")
@RequiredArgsConstructor
public class MarginSimulationController {

    private final MarginSimulationService simService;

    @PostMapping("/run")
    public MarginSimulationResult run(@RequestBody MarginSimulationRequest req) {
        return simService.simulate(req);
    }
}
