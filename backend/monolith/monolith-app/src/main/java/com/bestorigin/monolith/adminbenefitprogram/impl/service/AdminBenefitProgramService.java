package com.bestorigin.monolith.adminbenefitprogram.impl.service;

import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.AuditEventPage;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.BenefitProgramPage;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.BenefitProgramRequest;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.BenefitProgramResponse;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.BudgetRequest;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.BudgetResponse;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.DryRunRequest;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.DryRunResponse;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.IntegrationEventPage;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.ManualAdjustmentRequest;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.ManualAdjustmentResponse;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.ProgramStatusRequest;
import java.util.UUID;

public interface AdminBenefitProgramService {
    BenefitProgramPage searchPrograms(String token, String status, String catalogId, String type);

    BenefitProgramResponse createProgram(String token, String idempotencyKey, BenefitProgramRequest request);

    DryRunResponse dryRun(String token, UUID programId, DryRunRequest request);

    BenefitProgramResponse changeStatus(String token, UUID programId, ProgramStatusRequest request);

    BudgetResponse updateBudget(String token, UUID programId, BudgetRequest request);

    ManualAdjustmentResponse createManualAdjustment(String token, String idempotencyKey, UUID programId, ManualAdjustmentRequest request);

    AuditEventPage auditEvents(String token, UUID programId, String actionCode);

    IntegrationEventPage integrationEvents(String token, UUID programId, String targetContext);
}
