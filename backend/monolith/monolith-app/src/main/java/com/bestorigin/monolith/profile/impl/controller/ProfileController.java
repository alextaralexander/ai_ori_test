package com.bestorigin.monolith.profile.impl.controller;

import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileAddressUpsertRequest;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileAddressesResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileAuditEventsResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileContactCreateRequest;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileContactResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileContactsResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileDocumentUpsertRequest;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileDocumentsResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileErrorResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileGeneralResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileGeneralUpdateRequest;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileOverviewResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfilePasswordChangeRequest;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileReadinessFlow;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileReadinessResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileValidationReasonResponse;
import com.bestorigin.monolith.profile.impl.service.ProfileAccessDeniedException;
import com.bestorigin.monolith.profile.impl.service.ProfileService;
import com.bestorigin.monolith.profile.impl.service.ProfileValidationException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @GetMapping
    public ProfileOverviewResponse overview(@RequestHeader HttpHeaders headers) {
        return service.overview(userContext(headers), false);
    }

    @PutMapping("/general")
    public ProfileGeneralResponse updateGeneral(@RequestHeader HttpHeaders headers, @RequestBody ProfileGeneralUpdateRequest request) {
        return service.updateGeneral(userContext(headers), request);
    }

    @PostMapping("/contacts")
    public ProfileContactsResponse addContact(@RequestHeader HttpHeaders headers, @RequestBody ProfileContactCreateRequest request) {
        return service.addContact(userContext(headers), request, idempotencyKey(headers));
    }

    @PostMapping("/contacts/{contactId}/verification")
    public ProfileContactResponse startContactVerification(@RequestHeader HttpHeaders headers, @PathVariable UUID contactId) {
        return service.startContactVerification(userContext(headers), contactId);
    }

    @PostMapping("/addresses")
    public ProfileAddressesResponse addAddress(@RequestHeader HttpHeaders headers, @RequestBody ProfileAddressUpsertRequest request) {
        return service.addAddress(userContext(headers), request, idempotencyKey(headers));
    }

    @PutMapping("/addresses/{addressId}")
    public ProfileAddressesResponse updateAddress(@RequestHeader HttpHeaders headers, @PathVariable UUID addressId, @RequestBody ProfileAddressUpsertRequest request) {
        return service.updateAddress(userContext(headers), addressId, request);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@RequestHeader HttpHeaders headers, @PathVariable UUID addressId) {
        service.deleteAddress(userContext(headers), addressId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/documents")
    public ProfileDocumentsResponse upsertDocument(@RequestHeader HttpHeaders headers, @RequestBody ProfileDocumentUpsertRequest request) {
        return service.upsertDocument(userContext(headers), request, idempotencyKey(headers));
    }

    @PostMapping("/security/password")
    public ResponseEntity<Void> changePassword(@RequestHeader HttpHeaders headers, @RequestBody ProfilePasswordChangeRequest request) {
        service.changePassword(userContext(headers), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/audit-events")
    public ProfileAuditEventsResponse auditEvents(@RequestHeader HttpHeaders headers) {
        return service.auditEvents(userContext(headers));
    }

    @GetMapping("/readiness")
    public ProfileReadinessResponse readiness(@RequestHeader HttpHeaders headers, @RequestParam(required = false) ProfileReadinessFlow flow) {
        return service.readiness(userContext(headers), flow);
    }

    @GetMapping("/support/{userId}")
    public ProfileOverviewResponse supportOverview(
            @RequestHeader HttpHeaders headers,
            @PathVariable String userId,
            @RequestParam(required = false) String reason
    ) {
        return service.supportOverview(userContext(headers), userId, reason);
    }

    @ExceptionHandler(ProfileValidationException.class)
    public ResponseEntity<ProfileErrorResponse> handleValidation(ProfileValidationException ex) {
        HttpStatus status = ex.statusCode() == 409 ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(error(ex.getMessage()));
    }

    @ExceptionHandler(ProfileAccessDeniedException.class)
    public ResponseEntity<ProfileErrorResponse> handleForbidden(ProfileAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage()));
    }

    private static ProfileErrorResponse error(String code) {
        String target = code.contains("ADDRESS") ? "address" : code.contains("PASSWORD") ? "security" : "profile";
        String reason = code.equals("STR_MNEMO_PROFILE_ADDRESS_LOCKED") ? "ADDRESS_LOCKED_BY_ACTIVE_ORDER" : code;
        return new ProfileErrorResponse(code, List.of(new ProfileValidationReasonResponse(reason, "BLOCKING", target)), Map.of("reason", reason));
    }

    private static String userContext(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null || value.isBlank()) {
            return "anonymous";
        }
        return value.replace("Bearer ", "").trim();
    }

    private static String idempotencyKey(HttpHeaders headers) {
        String value = headers.getFirst("Idempotency-Key");
        return value == null || value.isBlank() ? "implicit-idempotency-key" : value;
    }
}
