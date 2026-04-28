package com.bestorigin.monolith.auth.impl.controller;

import com.bestorigin.monolith.auth.api.AuthDtos.AuthActivePartnerRequest;
import com.bestorigin.monolith.auth.api.AuthDtos.AuthErrorResponse;
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
import com.bestorigin.monolith.auth.impl.exception.AuthAccessDeniedException;
import com.bestorigin.monolith.auth.impl.exception.AuthSessionExpiredException;
import com.bestorigin.monolith.auth.impl.exception.AuthValidationException;
import com.bestorigin.monolith.auth.impl.service.AuthService;
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
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/test-login")
    public AuthSessionResponse testLogin(@RequestBody AuthLoginRequest request) {
        return service.login(request);
    }

    @PostMapping("/login")
    public AuthSessionResponse login(@RequestBody AuthLoginRequest request) {
        return service.login(request);
    }

    @GetMapping("/session")
    public AuthSessionResponse currentSession(@RequestHeader HttpHeaders headers) {
        return service.currentSession(token(headers));
    }

    @DeleteMapping("/session")
    public AuthLogoutResponse logout(@RequestHeader HttpHeaders headers) {
        return service.logout(token(headers));
    }

    @PostMapping("/session/route-access")
    public AuthRouteAccessResponse routeAccess(@RequestHeader HttpHeaders headers, @RequestBody AuthRouteAccessRequest request) {
        return service.routeAccess(token(headers), request);
    }

    @PostMapping("/invitation-code")
    public AuthInvitationCodeResponse invitationCode(@RequestHeader HttpHeaders headers, @RequestBody AuthInvitationCodeRequest request) {
        return service.saveInvitationCode(token(headers), request);
    }

    @GetMapping("/partners/search")
    public AuthPartnerSearchResponse partnerSearch(@RequestHeader HttpHeaders headers, @RequestParam String query) {
        return service.searchPartners(token(headers), query);
    }

    @PutMapping("/partners/active")
    public AuthSessionResponse activePartner(@RequestHeader HttpHeaders headers, @RequestBody AuthActivePartnerRequest request) {
        return service.setActivePartner(token(headers), request);
    }

    @PostMapping("/impersonation")
    public ResponseEntity<AuthImpersonationResponse> startImpersonation(
            @RequestHeader HttpHeaders headers,
            @RequestHeader(value = "X-Elevated-Session-Id", required = false) UUID elevatedSessionId,
            @RequestBody AuthImpersonationStartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.startImpersonation(token(headers), elevatedSessionId, request));
    }

    @PostMapping("/impersonation/{impersonationSessionId}/finish")
    public AuthSessionResponse finishImpersonation(@RequestHeader HttpHeaders headers, @PathVariable UUID impersonationSessionId) {
        return service.finishImpersonation(token(headers), impersonationSessionId);
    }

    @ExceptionHandler(AuthAccessDeniedException.class)
    public ResponseEntity<AuthErrorResponse> handleForbidden(AuthAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage()));
    }

    @ExceptionHandler(AuthSessionExpiredException.class)
    public ResponseEntity<AuthErrorResponse> handleExpired(AuthSessionExpiredException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error(ex.getMessage()));
    }

    @ExceptionHandler(AuthValidationException.class)
    public ResponseEntity<AuthErrorResponse> handleValidation(AuthValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage()));
    }

    private static AuthErrorResponse error(String code) {
        return new AuthErrorResponse(code, List.of(code), Map.of());
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replace("Bearer ", "").trim();
    }
}
