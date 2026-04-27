package com.bestorigin.monolith.mlmstructure.impl.controller;

import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmCommunityResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmConversionFunnelResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmDashboardResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmPartnerCardResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmStructureErrorResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmStructureValidationReasonResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmTeamActivityResponse;
import com.bestorigin.monolith.mlmstructure.api.MlmStructureDtos.MlmUpgradeResponse;
import com.bestorigin.monolith.mlmstructure.impl.service.MlmStructureAccessDeniedException;
import com.bestorigin.monolith.mlmstructure.impl.service.MlmStructureNotFoundException;
import com.bestorigin.monolith.mlmstructure.impl.service.MlmStructureService;
import com.bestorigin.monolith.mlmstructure.impl.service.MlmStructureValidationException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mlm-structure")
public class MlmStructureController {

    private final MlmStructureService service;

    public MlmStructureController(MlmStructureService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public MlmDashboardResponse dashboard(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String campaignId) {
        return service.dashboard(userContext(headers), campaignId);
    }

    @GetMapping("/community")
    public MlmCommunityResponse community(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) String campaignId,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) String status
    ) {
        return service.community(userContext(headers), campaignId, level, branchId, status);
    }

    @GetMapping("/conversion")
    public MlmConversionFunnelResponse conversion(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String campaignId) {
        return service.conversion(userContext(headers), campaignId);
    }

    @GetMapping("/team-activity")
    public MlmTeamActivityResponse teamActivity(
            @RequestHeader HttpHeaders headers,
            @RequestParam(required = false) String campaignId,
            @RequestParam(defaultValue = "false") boolean riskOnly
    ) {
        return service.teamActivity(userContext(headers), campaignId, riskOnly);
    }

    @GetMapping("/upgrade")
    public MlmUpgradeResponse upgrade(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String campaignId) {
        return service.upgrade(userContext(headers), campaignId);
    }

    @GetMapping("/partners/{personNumber}")
    public MlmPartnerCardResponse partnerCard(
            @RequestHeader HttpHeaders headers,
            @PathVariable String personNumber,
            @RequestParam(required = false) String campaignId
    ) {
        return service.partnerCard(userContext(headers), personNumber, campaignId);
    }

    @ExceptionHandler(MlmStructureAccessDeniedException.class)
    public ResponseEntity<MlmStructureErrorResponse> handleForbidden(MlmStructureAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage(), "mlm-structure"));
    }

    @ExceptionHandler(MlmStructureNotFoundException.class)
    public ResponseEntity<MlmStructureErrorResponse> handleNotFound(MlmStructureNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(ex.getMessage(), "mlm-structure"));
    }

    @ExceptionHandler(MlmStructureValidationException.class)
    public ResponseEntity<MlmStructureErrorResponse> handleValidation(MlmStructureValidationException ex) {
        return ResponseEntity.badRequest().body(error(ex.getMessage(), "mlm-structure"));
    }

    private static MlmStructureErrorResponse error(String code, String target) {
        return new MlmStructureErrorResponse(code, List.of(new MlmStructureValidationReasonResponse(code, "BLOCKING", target)), Map.of());
    }

    private static String userContext(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null || value.isBlank()) {
            return "anonymous";
        }
        return value.replace("Bearer ", "").trim();
    }
}
