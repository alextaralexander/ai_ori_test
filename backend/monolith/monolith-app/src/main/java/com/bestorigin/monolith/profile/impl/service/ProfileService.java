package com.bestorigin.monolith.profile.impl.service;

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
import java.util.UUID;

public interface ProfileService {

    ProfileOverviewResponse overview(String userContext, boolean auditRecorded);

    ProfileGeneralResponse updateGeneral(String userContext, ProfileGeneralUpdateRequest request);

    ProfileContactsResponse addContact(String userContext, ProfileContactCreateRequest request, String idempotencyKey);

    ProfileContactResponse startContactVerification(String userContext, UUID contactId);

    ProfileAddressesResponse addAddress(String userContext, ProfileAddressUpsertRequest request, String idempotencyKey);

    ProfileAddressesResponse updateAddress(String userContext, UUID addressId, ProfileAddressUpsertRequest request);

    void deleteAddress(String userContext, UUID addressId);

    ProfileDocumentsResponse upsertDocument(String userContext, ProfileDocumentUpsertRequest request, String idempotencyKey);

    void changePassword(String userContext, ProfilePasswordChangeRequest request);

    ProfileAuditEventsResponse auditEvents(String userContext);

    ProfileReadinessResponse readiness(String userContext, ProfileReadinessFlow flow);

    ProfileOverviewResponse supportOverview(String supportContext, String userId, String reason);
}
