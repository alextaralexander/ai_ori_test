package com.bestorigin.monolith.adminpim.impl.service;

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
import java.util.UUID;

public interface AdminPimService {

    WorkspaceResponse workspace(String token);

    CategoryListResponse searchCategories(String token, String status, String locale, String search);

    CategoryResponse createCategory(String token, CategoryUpsertRequest request);

    CategoryResponse updateCategory(String token, UUID categoryId, CategoryUpsertRequest request);

    CategoryResponse activateCategory(String token, UUID categoryId);

    ProductListResponse searchProducts(String token, String status, UUID categoryId, String brandCode, Boolean readyForPublication, String search);

    ProductDetailResponse createProduct(String token, ProductUpsertRequest request);

    ProductDetailResponse getProduct(String token, UUID productId);

    ProductDetailResponse updateProduct(String token, UUID productId, ProductUpsertRequest request);

    ProductDetailResponse publishProduct(String token, UUID productId, PublishRequest request);

    MediaResponse addMedia(String token, UUID productId, MediaCreateRequest request);

    MediaResponse approveMedia(String token, UUID mediaId);

    AttributeDefinitionResponse createAttribute(String token, AttributeDefinitionRequest request);

    RecommendationResponse createRecommendation(String token, RecommendationRequest request);

    ImportJobResponse importCatalog(String token, String idempotencyKey, ImportRequest request);

    ExportJobResponse exportCatalog(String token, ExportRequest request);

    AuditResponse audit(String token, String entityType, UUID entityId, String actionCode, String correlationId);
}
