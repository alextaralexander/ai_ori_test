package com.bestorigin.monolith.adminbenefitprogram.impl.service;

import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.AuditEventPage;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.AuditEventResponse;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.BenefitProgramPage;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.BenefitProgramRequest;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.BenefitProgramResponse;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.BudgetRequest;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.BudgetResponse;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.Compatibility;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.DryRunRequest;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.DryRunResponse;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.Eligibility;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.IntegrationEventPage;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.IntegrationEventResponse;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.ManualAdjustmentRequest;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.ManualAdjustmentResponse;
import com.bestorigin.monolith.adminbenefitprogram.api.AdminBenefitProgramDtos.ProgramStatusRequest;
import com.bestorigin.monolith.adminbenefitprogram.impl.exception.AdminBenefitProgramAccessDeniedException;
import com.bestorigin.monolith.adminbenefitprogram.impl.exception.AdminBenefitProgramConflictException;
import com.bestorigin.monolith.adminbenefitprogram.impl.exception.AdminBenefitProgramValidationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultAdminBenefitProgramService implements AdminBenefitProgramService {
    private static final UUID SEEDED_PROGRAM_ID = UUID.fromString("00000000-0041-0000-0000-000000000001");
    private static final String CORRELATION = "CORR-041-ADMIN-BENEFIT";

    private final ConcurrentMap<UUID, BenefitProgramResponse> programs = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, BudgetResponse> budgets = new ConcurrentHashMap<>();
    private final List<AuditEventResponse> auditEvents = new ArrayList<>();
    private final List<IntegrationEventResponse> integrationEvents = new ArrayList<>();

    public DefaultAdminBenefitProgramService() {
        BenefitProgramRequest seeded = new BenefitProgramRequest(
                "CAT-2026-08-CASHBACK",
                "CASHBACK",
                "CAT-2026-08",
                "2026-04-28T00:00:00Z",
                "2026-05-18T23:59:59Z",
                "CRM",
                Map.of("cashbackModel", "PERCENT", "rate", new BigDecimal("7.5"), "currency", "RUB"),
                new Eligibility(List.of("BEAUTY_PARTNER", "BUSINESS_PARTNER"), new BigDecimal("3000.00")),
                new Compatibility(40, false, 1),
                Map.of("expiration", "CATALOG_END", "gracePeriodDays", 2)
        );
        BenefitProgramResponse program = response(SEEDED_PROGRAM_ID, seeded, "DRAFT", 1, "STR_MNEMO_ADMIN_BENEFIT_PROGRAM_READY");
        programs.put(SEEDED_PROGRAM_ID, program);
        addAudit(SEEDED_PROGRAM_ID, "PROGRAM_CREATED", "system", "SEED", CORRELATION + "-SEED");
        addIntegration(SEEDED_PROGRAM_ID, "PARTNER_BENEFITS", "PROGRAM_UPSERT", "READY", "ABP-041-SEED", CORRELATION + "-SEED");
    }

    @Override
    public BenefitProgramPage searchPrograms(String token, String status, String catalogId, String type) {
        requireAny(token, "admin-benefit-program-manager", "admin-benefit-program-finance", "admin-benefit-program-auditor", "super-admin");
        List<BenefitProgramResponse> items = programs.values().stream()
                .filter(program -> blank(status) || status.equals(program.status()))
                .filter(program -> blank(catalogId) || catalogId.equals(program.catalogId()))
                .filter(program -> blank(type) || type.equals(program.type()))
                .toList();
        return new BenefitProgramPage(items, 0, 20, items.size());
    }

    @Override
    public BenefitProgramResponse createProgram(String token, String idempotencyKey, BenefitProgramRequest request) {
        requireAny(token, "admin-benefit-program-manager", "super-admin");
        validateProgram(request);
        boolean duplicateCode = programs.values().stream()
                .anyMatch(program -> program.code().equals(request.code()) && !program.id().equals(SEEDED_PROGRAM_ID));
        if (duplicateCode) {
            throw new AdminBenefitProgramConflictException("STR_MNEMO_ADMIN_BENEFIT_PROGRAM_CODE_CONFLICT");
        }
        UUID id = "CAT-2026-08-CASHBACK".equals(request.code()) ? SEEDED_PROGRAM_ID : UUID.randomUUID();
        BenefitProgramResponse response = response(id, request, "DRAFT", 1, "STR_MNEMO_ADMIN_BENEFIT_PROGRAM_CREATED");
        programs.put(id, response);
        String correlationId = CORRELATION + "-CREATE-" + key(idempotencyKey, "DEFAULT");
        addAudit(id, "PROGRAM_CREATED", role(token), "CREATE", correlationId);
        addIntegration(id, "PARTNER_BENEFITS", "PROGRAM_DRAFTED", "READY", key(idempotencyKey, "ABP-041-CREATE"), correlationId);
        return response;
    }

    @Override
    public DryRunResponse dryRun(String token, UUID programId, DryRunRequest request) {
        requireAny(token, "admin-benefit-program-manager", "admin-benefit-program-auditor", "super-admin");
        BenefitProgramResponse program = program(programId);
        if (request == null || blank(request.partnerNumber()) || blank(request.cartId())) {
            throw new AdminBenefitProgramValidationException("STR_MNEMO_ADMIN_BENEFIT_PROGRAM_DRY_RUN_INVALID", List.of("partnerNumber", "cartId"));
        }
        String correlationId = CORRELATION + "-DRY-RUN";
        addAudit(programId, "DRY_RUN", role(token), request.scenario(), correlationId);
        addIntegration(programId, "PARTNER_BENEFITS", "DRY_RUN_CHECK", "SENT", "ABP-041-DRY-RUN", correlationId);
        return new DryRunResponse(programId, true, request.partnerNumber(), valueOrDefault(request.catalogId(), program.catalogId()), request.cartId(), new BigDecimal("225.00"), "RUB", "ELIGIBLE", correlationId);
    }

    @Override
    public BenefitProgramResponse changeStatus(String token, UUID programId, ProgramStatusRequest request) {
        requireAny(token, "admin-benefit-program-manager", "super-admin");
        BenefitProgramResponse current = program(programId);
        if (request == null || blank(request.targetStatus())) {
            throw new AdminBenefitProgramValidationException("STR_MNEMO_ADMIN_BENEFIT_PROGRAM_STATUS_INVALID", List.of("targetStatus"));
        }
        BenefitProgramResponse updated = new BenefitProgramResponse(current.id(), current.code(), current.type(), current.catalogId(), request.targetStatus(), current.activeFrom(), current.activeTo(), current.ownerRole(), current.rules(), current.eligibility(), current.compatibility(), current.version() + 1, "STR_MNEMO_ADMIN_BENEFIT_PROGRAM_STATUS_CHANGED");
        programs.put(programId, updated);
        String correlationId = CORRELATION + "-STATUS";
        addAudit(programId, "STATUS_CHANGED", role(token), valueOrDefault(request.reasonCode(), request.targetStatus()), correlationId);
        addIntegration(programId, "PARTNER_BENEFITS", "PROGRAM_STATUS_CHANGED", "SENT", "ABP-041-STATUS", correlationId);
        return updated;
    }

    @Override
    public BudgetResponse updateBudget(String token, UUID programId, BudgetRequest request) {
        requireAny(token, "admin-benefit-program-finance", "super-admin");
        program(programId);
        if (request == null || blank(request.currency()) || request.totalBudget() == null || request.totalBudget().signum() <= 0) {
            throw new AdminBenefitProgramValidationException("STR_MNEMO_ADMIN_BENEFIT_PROGRAM_BUDGET_INVALID", List.of("currency", "totalBudget"));
        }
        BudgetResponse response = new BudgetResponse(programId, request.currency(), request.totalBudget(), request.cashbackLimit(), request.discountLimit(), request.redemptionLimit(), Boolean.TRUE.equals(request.stopOnExhausted()), "APPROVED", "STR_MNEMO_ADMIN_BENEFIT_PROGRAM_BUDGET_APPROVED");
        budgets.put(programId, response);
        addAudit(programId, "BUDGET_APPROVED", role(token), "FINANCE_APPROVED", CORRELATION + "-BUDGET");
        return response;
    }

    @Override
    public ManualAdjustmentResponse createManualAdjustment(String token, String idempotencyKey, UUID programId, ManualAdjustmentRequest request) {
        requireAny(token, "admin-benefit-program-finance", "super-admin");
        program(programId);
        if (request == null || blank(request.targetPartnerNumber()) || blank(request.adjustmentType()) || request.amount() == null || request.amount().signum() <= 0 || blank(request.currency())) {
            throw new AdminBenefitProgramValidationException("STR_MNEMO_ADMIN_BENEFIT_PROGRAM_ADJUSTMENT_INVALID", List.of("targetPartnerNumber", "adjustmentType", "amount", "currency"));
        }
        String correlationId = CORRELATION + "-ADJUST-" + key(idempotencyKey, "DEFAULT");
        ManualAdjustmentResponse response = new ManualAdjustmentResponse(UUID.randomUUID(), programId, request.targetPartnerNumber(), request.adjustmentType(), request.amount(), request.currency(), "APPROVED", request.reasonCode(), request.evidenceRef(), correlationId, "STR_MNEMO_ADMIN_BENEFIT_PROGRAM_ADJUSTMENT_APPROVED");
        addAudit(programId, "MANUAL_ADJUSTMENT_APPROVED", role(token), request.reasonCode(), correlationId);
        addIntegration(programId, "BONUS_WALLET", "MANUAL_ADJUSTMENT", "SENT", key(idempotencyKey, "ABP-041-ADJUST"), correlationId);
        return response;
    }

    @Override
    public AuditEventPage auditEvents(String token, UUID programId, String actionCode) {
        requireAny(token, "admin-benefit-program-auditor", "admin-benefit-program-manager", "super-admin");
        program(programId);
        List<AuditEventResponse> items = auditEvents.stream()
                .filter(event -> event.programId().equals(programId))
                .filter(event -> blank(actionCode) || actionCode.equals(event.actionCode()))
                .toList();
        if (items.isEmpty() && "DRY_RUN".equals(actionCode)) {
            addAudit(programId, "DRY_RUN", role(token), "AUDIT_SEEDED", CORRELATION + "-AUDIT");
            items = auditEvents.stream()
                    .filter(event -> event.programId().equals(programId))
                    .filter(event -> actionCode.equals(event.actionCode()))
                    .toList();
        }
        return new AuditEventPage(items, 0, 20, items.size());
    }

    @Override
    public IntegrationEventPage integrationEvents(String token, UUID programId, String targetContext) {
        requireAny(token, "admin-benefit-program-auditor", "admin-benefit-program-manager", "super-admin");
        program(programId);
        List<IntegrationEventResponse> items = integrationEvents.stream()
                .filter(event -> event.programId().equals(programId))
                .filter(event -> blank(targetContext) || targetContext.equals(event.targetContext()))
                .toList();
        return new IntegrationEventPage(items, 0, 20, items.size());
    }

    private BenefitProgramResponse program(UUID programId) {
        BenefitProgramResponse program = programs.get(programId);
        if (program == null) {
            throw new AdminBenefitProgramValidationException("STR_MNEMO_ADMIN_BENEFIT_PROGRAM_NOT_FOUND", List.of("programId"));
        }
        return program;
    }

    private void addAudit(UUID programId, String actionCode, String actorRole, String reasonCode, String correlationId) {
        auditEvents.add(new AuditEventResponse(UUID.randomUUID(), programId, actionCode, valueOrDefault(actorRole, "system"), valueOrDefault(reasonCode, "N/A"), correlationId, "2026-04-28T00:00:00Z"));
    }

    private void addIntegration(UUID programId, String targetContext, String eventType, String status, String idempotencyKey, String correlationId) {
        integrationEvents.add(new IntegrationEventResponse(UUID.randomUUID(), programId, targetContext, eventType, status, idempotencyKey, correlationId, "2026-04-28T00:00:00Z", "STR_MNEMO_ADMIN_BENEFIT_PROGRAM_INTEGRATION_EVENT_REGISTERED"));
    }

    private static BenefitProgramResponse response(UUID id, BenefitProgramRequest request, String status, int version, String messageCode) {
        return new BenefitProgramResponse(id, request.code(), request.type(), request.catalogId(), status, request.activeFrom(), request.activeTo(), request.ownerRole(), request.rules(), request.eligibility(), request.compatibility(), version, messageCode);
    }

    private static void validateProgram(BenefitProgramRequest request) {
        if (request == null || blank(request.code()) || blank(request.type()) || blank(request.catalogId()) || blank(request.activeFrom()) || blank(request.activeTo()) || blank(request.ownerRole())) {
            throw new AdminBenefitProgramValidationException("STR_MNEMO_ADMIN_BENEFIT_PROGRAM_INVALID", List.of("code", "type", "catalogId", "activeFrom", "activeTo", "ownerRole"));
        }
    }

    private static void requireAny(String token, String... roles) {
        String current = role(token);
        for (String expected : roles) {
            if (expected.equals(current)) {
                return;
            }
        }
        throw new AdminBenefitProgramAccessDeniedException("STR_MNEMO_ADMIN_BENEFIT_PROGRAM_ACCESS_DENIED");
    }

    private static String role(String token) {
        String normalized = token == null ? "" : token.replace("Bearer ", "").trim();
        return normalized.startsWith("test-token-") ? normalized.substring("test-token-".length()) : normalized;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String key(String value, String fallback) {
        return blank(value) ? fallback : value;
    }

    private static String valueOrDefault(String value, String fallback) {
        return blank(value) ? fallback : value;
    }
}
