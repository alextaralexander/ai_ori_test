package com.bestorigin.monolith.publiccontent.impl.service;

import com.bestorigin.monolith.publiccontent.api.Audience;
import com.bestorigin.monolith.publiccontent.api.ContentPageResponse;
import com.bestorigin.monolith.publiccontent.api.DocumentCollectionResponse;
import com.bestorigin.monolith.publiccontent.api.EntryPointResponse;
import com.bestorigin.monolith.publiccontent.api.FaqResponse;
import com.bestorigin.monolith.publiccontent.api.InfoSectionResponse;
import com.bestorigin.monolith.publiccontent.api.NavigationItemResponse;
import com.bestorigin.monolith.publiccontent.api.NewsFeedResponse;
import com.bestorigin.monolith.publiccontent.api.OfferResponse;
import com.bestorigin.monolith.publiccontent.api.PublicPageResponse;
import java.util.List;

public interface PublicContentService {

    PublicPageResponse getHomePage(Audience audience);

    PublicPageResponse getCommunityPage(Audience audience);

    List<NavigationItemResponse> getNavigation(Audience audience, String area);

    List<EntryPointResponse> getEntryPoints(Audience audience);

    NewsFeedResponse getNews(Audience audience);

    ContentPageResponse getContentPage(String contentId, Audience audience);

    OfferResponse getOffer(String offerId, Audience audience);

    FaqResponse getFaq(Audience audience, String category, String query);

    InfoSectionResponse getInfoSection(String section, Audience audience);

    DocumentCollectionResponse getDocuments(String documentType, Audience audience);
}
