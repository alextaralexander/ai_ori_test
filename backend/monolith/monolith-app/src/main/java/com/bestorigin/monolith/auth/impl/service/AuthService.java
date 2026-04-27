package com.bestorigin.monolith.auth.impl.service;

import com.bestorigin.monolith.auth.api.AuthDtos.AuthActivePartnerRequest;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthImpersonationResponse;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthImpersonationStartRequest;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthInvitationCodeRequest;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthInvitationCodeResponse;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthLoginRequest;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthLogoutResponse;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthPartnerSearchResponse;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthRouteAccessRequest;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthRouteAccessResponse;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthSessionResponse;
import java.util.UUID;

public interface AuthService {

    AuthSessionResponse login(AuthLoginRequest request);

    AuthSessionResponse currentSession(String token);

    AuthLogoutResponse logout(String token);

    AuthRouteAccessResponse routeAccess(String token, AuthRouteAccessRequest request);

    AuthInvitationCodeResponse saveInvitationCode(String token, AuthInvitationCodeRequest request);

    AuthPartnerSearchResponse searchPartners(String token, String query);

    AuthSessionResponse setActivePartner(String token, AuthActivePartnerRequest request);

    AuthImpersonationResponse startImpersonation(String token, UUID elevatedSessionId, AuthImpersonationStartRequest request);

    AuthSessionResponse finishImpersonation(String token, UUID impersonationSessionId);
}
