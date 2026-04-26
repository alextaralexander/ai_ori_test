package com.bestorigin.monolith.publiccontent.impl.service;

import com.bestorigin.monolith.publiccontent.api.Audience;
import com.bestorigin.monolith.publiccontent.api.BenefitLandingConversionRequest;
import com.bestorigin.monolith.publiccontent.api.BenefitLandingConversionResponse;
import com.bestorigin.monolith.publiccontent.api.BenefitLandingResponse;
import com.bestorigin.monolith.publiccontent.api.BenefitLandingType;
import com.bestorigin.monolith.publiccontent.api.ContentPageResponse;
import com.bestorigin.monolith.publiccontent.api.DocumentCollectionResponse;
import com.bestorigin.monolith.publiccontent.api.EntryPointResponse;
import com.bestorigin.monolith.publiccontent.api.FaqResponse;
import com.bestorigin.monolith.publiccontent.api.InfoSectionResponse;
import com.bestorigin.monolith.publiccontent.api.NavigationItemResponse;
import com.bestorigin.monolith.publiccontent.api.NewsFeedResponse;
import com.bestorigin.monolith.publiccontent.api.OfferResponse;
import com.bestorigin.monolith.publiccontent.api.PublicPageResponse;
import com.bestorigin.monolith.publiccontent.domain.PublicContentRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DefaultPublicContentService implements PublicContentService {

    private final PublicContentRepository repository;

    public DefaultPublicContentService(PublicContentRepository repository) {
        this.repository = repository;
    }

    @Override
    public PublicPageResponse getHomePage(Audience audience) {
        return repository.findPage("HOME", normalize(audience))
                .orElseThrow(() -> new PublicContentUnavailableException("STR_MNEMO_PUBLIC_CONTENT_UNAVAILABLE"));
    }

    @Override
    public PublicPageResponse getCommunityPage(Audience audience) {
        return repository.findPage("COMMUNITY", normalize(audience))
                .orElseThrow(() -> new PublicContentUnavailableException("STR_MNEMO_PUBLIC_CONTENT_UNAVAILABLE"));
    }

    @Override
    public List<NavigationItemResponse> getNavigation(Audience audience, String area) {
        return repository.findNavigation(normalize(audience), area);
    }

    @Override
    public List<EntryPointResponse> getEntryPoints(Audience audience) {
        return repository.findEntryPoints(normalize(audience));
    }

    @Override
    public NewsFeedResponse getNews(Audience audience) {
        return repository.findNews(normalize(audience));
    }

    @Override
    public ContentPageResponse getContentPage(String contentId, Audience audience) {
        return repository.findContentPage(contentId, normalize(audience))
                .orElseThrow(() -> new PublicContentNotFoundException("STR_MNEMO_PUBLIC_CONTENT_NOT_FOUND"));
    }

    @Override
    public OfferResponse getOffer(String offerId, Audience audience) {
        return repository.findOffer(offerId, normalize(audience))
                .orElseThrow(() -> new PublicContentNotFoundException("STR_MNEMO_PUBLIC_CONTENT_NOT_FOUND"));
    }

    @Override
    public FaqResponse getFaq(Audience audience, String category, String query) {
        return repository.findFaq(normalize(audience), category, query);
    }

    @Override
    public InfoSectionResponse getInfoSection(String section, Audience audience) {
        return repository.findInfoSection(section, normalize(audience))
                .orElseThrow(() -> new PublicContentNotFoundException("STR_MNEMO_PUBLIC_INFO_NOT_FOUND"));
    }

    @Override
    public DocumentCollectionResponse getDocuments(String documentType, Audience audience) {
        return repository.findDocuments(documentType, normalize(audience))
                .orElseThrow(() -> new PublicContentNotFoundException("STR_MNEMO_PUBLIC_DOCUMENTS_NOT_FOUND"));
    }

    @Override
    public BenefitLandingResponse getBenefitLanding(
            BenefitLandingType landingType,
            String code,
            String campaignId,
            String variant
    ) {
        return repository.findBenefitLanding(landingType, code, campaignId, variant)
                .orElseThrow(() -> new PublicContentNotFoundException("STR_MNEMO_PUBLIC_BENEFIT_LANDING_NOT_FOUND"));
    }

    @Override
    public BenefitLandingConversionResponse registerBenefitLandingConversion(BenefitLandingConversionRequest request) {
        repository.saveBenefitLandingConversion(
                request.landingType(),
                request.variant(),
                request.referralCode(),
                request.campaignId(),
                request.ctaType(),
                request.routePath(),
                request.occurredAt(),
                request.anonymousSessionId()
        );
        return new BenefitLandingConversionResponse(true);
    }

    private static Audience normalize(Audience audience) {
        return audience == null ? Audience.GUEST : audience;
    }
}
