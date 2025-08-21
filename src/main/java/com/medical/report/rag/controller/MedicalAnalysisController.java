package com.medical.report.rag.controller;

import com.medical.report.rag.dto.QueryDto;
import com.medical.report.rag.service.MedicalAnalysisService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MedicalAnalysisController {
    private final MedicalAnalysisService medicalAnalysisService;

    public MedicalAnalysisController(MedicalAnalysisService medicalAnalysisService) {
        this.medicalAnalysisService = medicalAnalysisService;
    }

    @PostMapping("/medical-analysis")
    public String analyzeMedicalReport(@RequestBody QueryDto queryDto) {
        return medicalAnalysisService.analyze(queryDto);
    }
}
