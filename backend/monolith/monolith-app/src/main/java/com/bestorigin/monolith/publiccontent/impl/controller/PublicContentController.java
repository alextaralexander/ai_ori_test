package com.bestorigin.monolith.publiccontent.impl.controller;

import com.bestorigin.monolith.publiccontent.api.Audience;
import com.bestorigin.monolith.publiccontent.api.ContentPageResponse;
import com.bestorigin.monolith.publiccontent.api.DocumentCollectionResponse;
import com.bestorigin.monolith.publiccontent.api.EntryPointResponse;
import com.bestorigin.monolith.publiccontent.api.ErrorResponse;
import com.bestorigin.monolith.publiccontent.api.FaqResponse;
import com.bestorigin.monolith.publiccontent.api.InfoSectionResponse;
import com.bestorigin.monolith.publiccontent.api.NavigationItemResponse;
import com.bestorigin.monolith.publiccontent.api.NewsFeedResponse;
import com.bestorigin.monolith.publiccontent.api.OfferResponse;
import com.bestorigin.monolith.publiccontent.api.PublicPageResponse;
import com.bestorigin.monolith.publiccontent.impl.service.PublicContentNotFoundException;
import com.bestorigin.monolith.publiccontent.impl.service.PublicContentService;
import com.bestorigin.monolith.publiccontent.impl.service.PublicContentUnavailableException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public-content")
public class PublicContentController {

    private final PublicContentService service;

    public PublicContentController(PublicContentService service) {
        this.service = service;
    }

    @GetMapping("/pages/home")
    public PublicPageResponse getHomePage(@RequestParam(defaultValue = "GUEST") Audience audience) {
        return service.getHomePage(audience);
    }

    @GetMapping("/pages/community")
    public PublicPageResponse getCommunityPage(@RequestParam(defaultValue = "GUEST") Audience audience) {
        return service.getCommunityPage(audience);
    }

    @GetMapping("/navigation")
    public Map<String, List<NavigationItemResponse>> getNavigation(
            @RequestParam(defaultValue = "GUEST") Audience audience,
            @RequestParam(required = false) String area
    ) {
        return Map.of("items", service.getNavigation(audience, area));
    }

    @GetMapping("/entry-points")
    public Map<String, List<EntryPointResponse>> getEntryPoints(@RequestParam(defaultValue = "GUEST") Audience audience) {
        return Map.of("items", service.getEntryPoints(audience));
    }

    @GetMapping("/news")
    public NewsFeedResponse getNews(@RequestParam(defaultValue = "GUEST") Audience audience) {
        return service.getNews(audience);
    }

    @GetMapping("/content/{contentId}")
    public ContentPageResponse getContentPage(
            @PathVariable String contentId,
            @RequestParam(defaultValue = "GUEST") Audience audience
    ) {
        return service.getContentPage(contentId, audience);
    }

    @GetMapping("/offers/{offerId}")
    public OfferResponse getOffer(
            @PathVariable String offerId,
            @RequestParam(defaultValue = "GUEST") Audience audience
    ) {
        return service.getOffer(offerId, audience);
    }

    @GetMapping("/faq")
    public FaqResponse getFaq(
            @RequestParam(defaultValue = "GUEST") Audience audience,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String query
    ) {
        return service.getFaq(audience, category, query);
    }

    @GetMapping("/info/{section}")
    public InfoSectionResponse getInfoSection(
            @PathVariable String section,
            @RequestParam(defaultValue = "GUEST") Audience audience
    ) {
        return service.getInfoSection(section, audience);
    }

    @GetMapping("/documents/{documentType}")
    public DocumentCollectionResponse getDocuments(
            @PathVariable String documentType,
            @RequestParam(defaultValue = "GUEST") Audience audience
    ) {
        return service.getDocuments(documentType, audience);
    }

    @ExceptionHandler(PublicContentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(PublicContentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(PublicContentUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleUnavailable(PublicContentUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorResponse(ex.getMessage()));
    }
}
