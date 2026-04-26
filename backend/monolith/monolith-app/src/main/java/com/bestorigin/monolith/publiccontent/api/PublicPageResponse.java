package com.bestorigin.monolith.publiccontent.api;

import java.util.List;

public record PublicPageResponse(
        String pageKey,
        String routePath,
        String titleKey,
        List<ContentBlockResponse> blocks,
        List<NavigationItemResponse> navigation,
        List<EntryPointResponse> entryPoints
) {
}
