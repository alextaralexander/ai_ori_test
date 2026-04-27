package com.bestorigin.monolith.profile.impl.service;

import com.bestorigin.monolith.profile.api.ProfileDtos.ContactType;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileAddressResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileAddressUpsertRequest;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileAddressesResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileAuditEventResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileAuditEventsResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileContactCreateRequest;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileContactResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileContactsResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileDocumentResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileDocumentUpsertRequest;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileDocumentsResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileGeneralResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileGeneralUpdateRequest;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileOverviewResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfilePasswordChangeRequest;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileReadinessFlow;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileReadinessResponse;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileSectionStatus;
import com.bestorigin.monolith.profile.api.ProfileDtos.ProfileSectionSummary;
import com.bestorigin.monolith.profile.api.ProfileDtos.VerificationStatus;
import com.bestorigin.monolith.profile.domain.ProfileRepository;
import com.bestorigin.monolith.profile.domain.ProfileSnapshot;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DefaultProfileService implements ProfileService {

    private static final UUID CONTACT_ID = UUID.fromString("00000000-0000-0000-0000-000000000013");
    private static final UUID ADDRESS_ID = UUID.fromString("00000000-0000-0000-0000-000000000013");
    private static final UUID DOCUMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000014");

    private final ProfileRepository repository;

    public DefaultProfileService(ProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProfileOverviewResponse overview(String userContext, boolean auditRecorded) {
        ProfileSnapshot profile = repository.findOrCreate(userContext);
        return new ProfileOverviewResponse(
                profile.profileId(),
                profile.ownerUserId(),
                List.of(
                        new ProfileSectionSummary("GENERAL", ProfileSectionStatus.COMPLETE, List.of(), null),
                        new ProfileSectionSummary("CONTACTS", ProfileSectionStatus.REQUIRES_VERIFICATION, List.of("emailVerification"), "STR_MNEMO_PROFILE_CONTACT_REQUIRES_VERIFICATION"),
                        new ProfileSectionSummary("ADDRESSES", ProfileSectionStatus.COMPLETE, List.of(), null),
                        new ProfileSectionSummary("DOCUMENTS", ProfileSectionStatus.COMPLETE, List.of(), null),
                        new ProfileSectionSummary("SECURITY", ProfileSectionStatus.COMPLETE, List.of(), null)
                ),
                List.of(readiness(userContext, ProfileReadinessFlow.CHECKOUT), readiness(userContext, ProfileReadinessFlow.DELIVERY), readiness(userContext, ProfileReadinessFlow.CLAIM)),
                auditRecorded
        );
    }

    @Override
    public ProfileGeneralResponse updateGeneral(String userContext, ProfileGeneralUpdateRequest request) {
        if (request.firstName() == null || request.firstName().isBlank() || request.lastName() == null || request.lastName().isBlank()) {
            throw new ProfileValidationException("STR_MNEMO_PROFILE_GENERAL_REQUIRED", 400);
        }
        repository.save(new ProfileSnapshot(
                repository.findOrCreate(userContext).profileId(),
                userContext,
                request.firstName(),
                request.lastName(),
                request.preferredLanguage() == null ? "ru" : request.preferredLanguage()
        ));
        return new ProfileGeneralResponse(request.firstName(), request.lastName(), request.middleName(), request.birthDate(), request.gender(), request.preferredLanguage());
    }

    @Override
    public ProfileContactsResponse addContact(String userContext, ProfileContactCreateRequest request, String idempotencyKey) {
        ContactType type = request.contactType() == null ? ContactType.EMAIL : request.contactType();
        return new ProfileContactsResponse(List.of(contact(type, type == ContactType.EMAIL ? "c********3@example.test" : "+7******0013")));
    }

    @Override
    public ProfileContactResponse startContactVerification(String userContext, UUID contactId) {
        return new ProfileContactResponse(CONTACT_ID, ContactType.EMAIL, "c********3@example.test", true, VerificationStatus.REQUIRES_VERIFICATION, "STR_MNEMO_PROFILE_CONTACT_REQUIRES_VERIFICATION");
    }

    @Override
    public ProfileAddressesResponse addAddress(String userContext, ProfileAddressUpsertRequest request, String idempotencyKey) {
        return new ProfileAddressesResponse(List.of(address(request.city(), request.street(), request.house(), request.postalCode(), true, null)));
    }

    @Override
    public ProfileAddressesResponse updateAddress(String userContext, UUID addressId, ProfileAddressUpsertRequest request) {
        return addAddress(userContext, request, "update-" + addressId);
    }

    @Override
    public void deleteAddress(String userContext, UUID addressId) {
        throw new ProfileValidationException("STR_MNEMO_PROFILE_ADDRESS_LOCKED", 409);
    }

    @Override
    public ProfileDocumentsResponse upsertDocument(String userContext, ProfileDocumentUpsertRequest request, String idempotencyKey) {
        return new ProfileDocumentsResponse(List.of(new ProfileDocumentResponse(
                DOCUMENT_ID,
                request.documentType() == null ? "PASSPORT" : request.documentType(),
                "45********56",
                true,
                VerificationStatus.UNVERIFIED
        )));
    }

    @Override
    public void changePassword(String userContext, ProfilePasswordChangeRequest request) {
        if (request.newPassword() == null || request.newPassword().length() < 8) {
            throw new ProfileValidationException("STR_MNEMO_PROFILE_PASSWORD_WEAK", 400);
        }
    }

    @Override
    public ProfileAuditEventsResponse auditEvents(String userContext) {
        return new ProfileAuditEventsResponse(List.of(
                new ProfileAuditEventResponse("GENERAL", "lastName", "USER", "PROFILE_UPDATE", "П********ь", "И*****а", OffsetDateTime.now().toString()),
                new ProfileAuditEventResponse("DOCUMENTS", "documentNumber", "USER", "DOCUMENT_UPDATE", "45********00", "45********56", OffsetDateTime.now().toString())
        ));
    }

    @Override
    public ProfileReadinessResponse readiness(String userContext, ProfileReadinessFlow flow) {
        ProfileReadinessFlow actualFlow = flow == null ? ProfileReadinessFlow.CHECKOUT : flow;
        return new ProfileReadinessResponse(actualFlow, true, List.of(), null);
    }

    @Override
    public ProfileOverviewResponse supportOverview(String supportContext, String userId, String reason) {
        if (!supportContext.startsWith("support-")) {
            throw new ProfileAccessDeniedException("STR_MNEMO_PROFILE_ACCESS_DENIED");
        }
        if (reason == null || reason.isBlank()) {
            throw new ProfileValidationException("STR_MNEMO_PROFILE_SUPPORT_REASON_REQUIRED", 400);
        }
        return overview(userId, true);
    }

    private static ProfileContactResponse contact(ContactType type, String maskedValue) {
        return new ProfileContactResponse(CONTACT_ID, type, maskedValue, true, VerificationStatus.REQUIRES_VERIFICATION, "STR_MNEMO_PROFILE_CONTACT_REQUIRES_VERIFICATION");
    }

    private static ProfileAddressResponse address(String city, String street, String house, String postalCode, boolean defaultAddress, String lockReason) {
        return new ProfileAddressResponse(
                ADDRESS_ID,
                city == null ? "Москва" : city,
                street == null ? "Тверская" : street,
                house == null ? "1" : house,
                postalCode == null ? "101000" : postalCode,
                defaultAddress,
                lockReason,
                lockReason == null ? null : "STR_MNEMO_PROFILE_ADDRESS_LOCKED"
        );
    }
}
