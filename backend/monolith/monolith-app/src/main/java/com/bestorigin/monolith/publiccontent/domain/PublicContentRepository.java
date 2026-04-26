package com.bestorigin.monolith.publiccontent.domain;

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
import java.util.Optional;

public interface PublicContentRepository {

    Optional<PublicPageResponse> findPage(String pageKey, Audience audience);

    List<NavigationItemResponse> findNavigation(Audience audience, String area);

    List<EntryPointResponse> findEntryPoints(Audience audience);

    NewsFeedResponse findNews(Audience audience);

    Optional<ContentPageResponse> findContentPage(String contentId, Audience audience);

    Optional<OfferResponse> findOffer(String offerId, Audience audience);

    FaqResponse findFaq(Audience audience, String category, String query);

    Optional<InfoSectionResponse> findInfoSection(String section, Audience audience);

    Optional<DocumentCollectionResponse> findDocuments(String documentType, Audience audience);
}
