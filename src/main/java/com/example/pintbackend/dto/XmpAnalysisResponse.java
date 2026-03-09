package com.example.pintbackend.dto;

import java.util.Map;

/**
 * XMP 분석 결과 중, 실제 조정값이 있는 3개 카테고리만 담는 응답 DTO.
 */
public record XmpAnalysisResponse(
        Map<String, String> basicAdjustments,
        Map<String, String> colorAdjustments,
        Map<String, String> detailAdjustments
) {
}
