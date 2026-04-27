package com.bestorigin.monolith.adminpim.impl.controller;

import com.bestorigin.monolith.adminpim.api.AdminPimDtos.AdminPimErrorResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.AttributeDefinitionRequest;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.AttributeDefinitionResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.AuditResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.CategoryListResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.CategoryResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.CategoryUpsertRequest;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.ExportJobResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.ExportRequest;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.ImportJobResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.ImportRequest;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.MediaCreateRequest;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.MediaResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.ProductDetailResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.ProductListResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.ProductUpsertRequest;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.PublishRequest;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.RecommendationRequest;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.RecommendationResponse;
import com.bestorigin.monolith.adminpim.api.AdminPimDtos.WorkspaceResponse;
import com.bestorigin.monolith.adminpim.impl.exception.AdminPimAccessDeniedException;
import com.bestorigin.monolith.adminpim.impl.exception.AdminPimConflictException;
import com.bestorigin.monolith.adminpim.impl.exception.AdminPimValidationException;
import com.bestorigin.monolith.adminpim.impl.service.AdminPimService;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/admin-pim")
public class AdminPimController {

    private final AdminPimService service;

    public AdminPimController(AdminPimService service) {
        this.service = service;
    }

    @GetMapping("/workspace")
    public WorkspaceResponse workspace(@RequestHeader HttpHeaders headers) {
        return service.workspace(token(headers));
    }

    @GetMapping("/categories")
    public CategoryListResponse categories(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String status, @RequestParam(required = false) String locale, @RequestParam(required = false) String search) {
        return service.searchCategories(token(headers), status, locale, search);
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@RequestHeader HttpHeaders headers, @RequestBody CategoryUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createCategory(token(headers), request));
    }

    @PutMapping("/categories/{categoryId}")
    public CategoryResponse updateCategory(@RequestHeader HttpHeaders headers, @PathVariable UUID categoryId, @RequestBody CategoryUpsertRequest request) {
        return service.updateCategory(token(headers), categoryId, request);
    }

    @PostMapping("/categories/{categoryId}/activate")
    public CategoryResponse activateCategory(@RequestHeader HttpHeaders headers, @PathVariable UUID categoryId) {
        return service.activateCategory(token(headers), categoryId);
    }

    @GetMapping("/products")
    public ProductListResponse products(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String status, @RequestParam(required = false) UUID categoryId, @RequestParam(required = false) String brandCode, @RequestParam(required = false) Boolean readyForPublication, @RequestParam(required = false) String search) {
        return service.searchProducts(token(headers), status, categoryId, brandCode, readyForPublication, search);
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDetailResponse> createProduct(@RequestHeader HttpHeaders headers, @RequestBody ProductUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createProduct(token(headers), request));
    }

    @GetMapping("/products/{productId}")
    public ProductDetailResponse product(@RequestHeader HttpHeaders headers, @PathVariable UUID productId) {
        return service.getProduct(token(headers), productId);
    }

    @PutMapping("/products/{productId}")
    public ProductDetailResponse updateProduct(@RequestHeader HttpHeaders headers, @PathVariable UUID productId, @RequestBody ProductUpsertRequest request) {
        return service.updateProduct(token(headers), productId, request);
    }

    @PostMapping("/products/{productId}/publish")
    public ProductDetailResponse publishProduct(@RequestHeader HttpHeaders headers, @PathVariable UUID productId, @RequestBody(required = false) PublishRequest request) {
        return service.publishProduct(token(headers), productId, request);
    }

    @PostMapping("/products/{productId}/media")
    public ResponseEntity<MediaResponse> addMedia(@RequestHeader HttpHeaders headers, @PathVariable UUID productId, @RequestBody MediaCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addMedia(token(headers), productId, request));
    }

    @PostMapping("/media/{mediaId}/approve")
    public MediaResponse approveMedia(@RequestHeader HttpHeaders headers, @PathVariable UUID mediaId) {
        return service.approveMedia(token(headers), mediaId);
    }

    @PostMapping("/attributes")
    public ResponseEntity<AttributeDefinitionResponse> createAttribute(@RequestHeader HttpHeaders headers, @RequestBody AttributeDefinitionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAttribute(token(headers), request));
    }

    @PostMapping("/recommendations")
    public ResponseEntity<RecommendationResponse> createRecommendation(@RequestHeader HttpHeaders headers, @RequestBody RecommendationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createRecommendation(token(headers), request));
    }

    @PostMapping("/imports")
    public ResponseEntity<ImportJobResponse> importCatalog(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody ImportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.importCatalog(token(headers), idempotencyKey, request));
    }

    @PostMapping("/exports")
    public ResponseEntity<ExportJobResponse> exportCatalog(@RequestHeader HttpHeaders headers, @RequestBody ExportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.exportCatalog(token(headers), request));
    }

    @GetMapping("/audit")
    public AuditResponse audit(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String entityType, @RequestParam(required = false) UUID entityId, @RequestParam(required = false) String actionCode, @RequestParam(required = false) String correlationId) {
        return service.audit(token(headers), entityType, entityId, actionCode, correlationId);
    }

    @ExceptionHandler(AdminPimAccessDeniedException.class)
    public ResponseEntity<AdminPimErrorResponse> handleForbidden(AdminPimAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage(), null));
    }

    @ExceptionHandler(AdminPimConflictException.class)
    public ResponseEntity<AdminPimErrorResponse> handleConflict(AdminPimConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage(), null));
    }

    @ExceptionHandler(AdminPimValidationException.class)
    public ResponseEntity<AdminPimErrorResponse> handleValidation(AdminPimValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage(), ex.details()));
    }

    private static AdminPimErrorResponse error(String messageCode, java.util.List<String> details) {
        return new AdminPimErrorResponse(messageCode, "CORR-029-ERROR", details == null ? java.util.List.of() : details);
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replace("Bearer ", "").trim();
    }
}
