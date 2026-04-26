package com.bestorigin.monolith.publiccontent.impl.service;

import com.bestorigin.monolith.publiccontent.api.Audience;
import com.bestorigin.monolith.publiccontent.api.EntryPointResponse;
import com.bestorigin.monolith.publiccontent.api.NavigationItemResponse;
import com.bestorigin.monolith.publiccontent.api.PublicPageResponse;
import java.util.List;

public interface PublicContentService {

    PublicPageResponse getHomePage(Audience audience);

    PublicPageResponse getCommunityPage(Audience audience);

    List<NavigationItemResponse> getNavigation(Audience audience, String area);

    List<EntryPointResponse> getEntryPoints(Audience audience);
}
