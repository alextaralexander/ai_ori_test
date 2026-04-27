package com.bestorigin.monolith.profile.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ProfileDtos {

    private ProfileDtos() {
    }

    public enum ProfileSectionStatus {
        COMPLETE,
        INCOMPLETE,
        REQUIRES_VERIFICATION,
        LOCKED
    }

    public enum ProfileReadinessFlow {
        CHECKOUT,
        DELIVERY,
        CLAIM
    }

    public enum ContactType {
        EMAIL,
        PHONE
    }

    public enum VerificationStatus {
        UNVERIFIED,
        REQUIRES_VERIFICATION,
        VERIFIED,
        FAILED
    }

    public record ProfileOverviewResponse(
            UUID profileId,
            String ownerUserId,
            List<ProfileSectionSummary> sections,
            List<ProfileReadinessResponse> readiness,
            Boolean auditRecorded
    ) {
    }

    public record ProfileSectionSummary(
            String sectionKey,
            ProfileSectionStatus status,
            List<String> missingFields,
            String messageMnemo
    ) {
    }

    public record ProfileGeneralUpdateRequest(
            String firstName,
            String lastName,
            String middleName,
            String birthDate,
            String gender,
            String preferredLanguage
    ) {
    }

    public record ProfileGeneralResponse(
            String firstName,
            String lastName,
            String middleName,
            String birthDate,
            String gender,
            String preferredLanguage
    ) {
    }

    public record ProfileContactCreateRequest(
            ContactType contactType,
            String value,
            Boolean primary
    ) {
    }

    public record ProfileContactResponse(
            UUID contactId,
            ContactType contactType,
            String maskedValue,
            boolean primary,
            VerificationStatus verificationStatus,
            String messageMnemo
    ) {
    }

    public record ProfileContactsResponse(List<ProfileContactResponse> contacts) {
    }

    public record ProfileAddressUpsertRequest(
            String countryCode,
            String region,
            String city,
            String street,
            String house,
            String building,
            String apartment,
            String postalCode,
            String deliveryComment,
            @JsonProperty("default")
            Boolean defaultAddress
    ) {
    }

    public record ProfileAddressResponse(
            UUID addressId,
            String city,
            String street,
            String house,
            String postalCode,
            @JsonProperty("default")
            boolean defaultAddress,
            String lockReason,
            String messageMnemo
    ) {
    }

    public record ProfileAddressesResponse(List<ProfileAddressResponse> addresses) {
    }

    public record ProfileDocumentUpsertRequest(
            String documentType,
            Map<String, Object> documentPayload
    ) {
    }

    public record ProfileDocumentResponse(
            UUID documentId,
            String documentType,
            String documentNumberMasked,
            boolean active,
            VerificationStatus verificationStatus
    ) {
    }

    public record ProfileDocumentsResponse(List<ProfileDocumentResponse> documents) {
    }

    public record ProfilePasswordChangeRequest(
            String currentPassword,
            String newPassword
    ) {
    }

    public record ProfileAuditEventResponse(
            String sectionKey,
            String fieldKey,
            String actorType,
            String businessReason,
            String oldValueMasked,
            String newValueMasked,
            String occurredAt
    ) {
    }

    public record ProfileAuditEventsResponse(List<ProfileAuditEventResponse> events) {
    }

    public record ProfileReadinessResponse(
            ProfileReadinessFlow flow,
            boolean ready,
            List<String> missingFields,
            String messageMnemo
    ) {
    }

    public record ProfileErrorResponse(
            String code,
            List<ProfileValidationReasonResponse> details,
            Map<String, String> metadata
    ) {
    }

    public record ProfileValidationReasonResponse(
            String code,
            String severity,
            String target
    ) {
    }
}
