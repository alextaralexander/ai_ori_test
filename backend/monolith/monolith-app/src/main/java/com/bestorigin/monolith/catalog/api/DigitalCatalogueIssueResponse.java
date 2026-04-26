package com.bestorigin.monolith.catalog.api;

import java.time.LocalDate;
import java.util.List;

public record DigitalCatalogueIssueResponse(
        String issueCode,
        String title,
        String periodType,
        Period period,
        String publicationStatus,
        ViewerCapabilities viewer,
        List<Page> pages,
        List<Material> materials
) {
    public record Period(LocalDate startDate, LocalDate endDate) {
    }

    public record ViewerCapabilities(boolean zoom, boolean download, boolean share) {
    }

    public record Page(
            int pageNumber,
            String imageUrl,
            String thumbnailUrl,
            int widthPx,
            int heightPx,
            List<Hotspot> hotspots
    ) {
    }

    public record Hotspot(
            String productCode,
            double xPercent,
            double yPercent,
            double widthPercent,
            double heightPercent
    ) {
    }

    public record Material(
            String materialId,
            String materialType,
            String title,
            Long fileSizeBytes,
            String publicationStatus,
            String previewUrl,
            MaterialActions actions
    ) {
    }

    public record MaterialActions(boolean canOpen, boolean canDownload, boolean canShare) {
    }
}
