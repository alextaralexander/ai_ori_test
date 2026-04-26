package com.bestorigin.monolith.publiccontent.impl.service;

import com.bestorigin.monolith.publiccontent.api.Audience;
import com.bestorigin.monolith.publiccontent.api.BreadcrumbResponse;
import com.bestorigin.monolith.publiccontent.api.ContentAttachmentResponse;
import com.bestorigin.monolith.publiccontent.api.ContentBlockResponse;
import com.bestorigin.monolith.publiccontent.api.ContentCtaResponse;
import com.bestorigin.monolith.publiccontent.api.ContentPageResponse;
import com.bestorigin.monolith.publiccontent.api.ContentSectionResponse;
import com.bestorigin.monolith.publiccontent.api.DocumentCollectionResponse;
import com.bestorigin.monolith.publiccontent.api.DocumentResponse;
import com.bestorigin.monolith.publiccontent.api.DocumentVersionResponse;
import com.bestorigin.monolith.publiccontent.api.EntryPointResponse;
import com.bestorigin.monolith.publiccontent.api.FaqCategoryResponse;
import com.bestorigin.monolith.publiccontent.api.FaqItemResponse;
import com.bestorigin.monolith.publiccontent.api.FaqResponse;
import com.bestorigin.monolith.publiccontent.api.InfoRelatedDocumentResponse;
import com.bestorigin.monolith.publiccontent.api.InfoSectionResponse;
import com.bestorigin.monolith.publiccontent.api.NavigationItemResponse;
import com.bestorigin.monolith.publiccontent.api.NewsFeedResponse;
import com.bestorigin.monolith.publiccontent.api.NewsItemResponse;
import com.bestorigin.monolith.publiccontent.api.OfferHeroResponse;
import com.bestorigin.monolith.publiccontent.api.OfferResponse;
import com.bestorigin.monolith.publiccontent.api.ProductLinkResponse;
import com.bestorigin.monolith.publiccontent.api.PublicPageResponse;
import com.bestorigin.monolith.publiccontent.api.SeoMetadataResponse;
import com.bestorigin.monolith.publiccontent.domain.PublicContentRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPublicContentRepository implements PublicContentRepository {

    @Override
    public Optional<PublicPageResponse> findPage(String pageKey, Audience audience) {
        List<ContentBlockResponse> blocks = "COMMUNITY".equals(pageKey) ? communityBlocks() : homeBlocks();
        String route = "COMMUNITY".equals(pageKey) ? "/community" : "/home";
        return Optional.of(new PublicPageResponse(
                pageKey,
                route,
                "public." + pageKey.toLowerCase() + ".title",
                blocks,
                findNavigation(audience, null),
                findEntryPoints(audience)
        ));
    }

    @Override
    public List<NavigationItemResponse> findNavigation(Audience audience, String area) {
        List<NavigationItemResponse> items = List.of(
                item("catalog", "public.navigation.catalog", "/catalog", "HEADER"),
                item("news", "public.navigation.news", "/news", "HEADER"),
                item("faq", "public.navigation.faq", "/faq", "HEADER"),
                item("community", "public.navigation.community", "/community", "HEADER"),
                item("benefits", "public.navigation.benefits", "/benefits", "HEADER"),
                item("documents", "public.navigation.documents", "/documents/terms", "FOOTER"),
                item("contacts", "public.navigation.contacts", "/contacts", "FOOTER"),
                item("register", "public.navigation.register", "/register", "HEADER"),
                item("cart", "public.navigation.cart", "/cart", "HEADER")
        );
        if (area == null || area.isBlank()) {
            return items;
        }
        return items.stream().filter(item -> area.equals(item.area())).toList();
    }

    @Override
    public List<EntryPointResponse> findEntryPoints(Audience audience) {
        List<EntryPointResponse> common = List.of(
                entry("catalog", "public.entry.catalog", "public.entry.catalog.description", "/catalog", Audience.ANY),
                entry("community", "public.entry.community", "public.entry.community.description", "/community", Audience.ANY),
                entry("benefits", "public.entry.benefits", "public.entry.benefits.description", "/benefits", Audience.ANY),
                entry("register", "public.entry.register", "public.entry.register.description", "/register", Audience.GUEST)
        );
        List<EntryPointResponse> personal = switch (audience) {
            case CUSTOMER -> List.of(
                    entry("profile", "public.entry.profile", "public.entry.profile.description", "/profile", Audience.CUSTOMER),
                    entry("cart", "public.entry.cart", "public.entry.cart.description", "/cart", Audience.CUSTOMER),
                    entry("orders", "public.entry.orders", "public.entry.orders.description", "/orders", Audience.CUSTOMER)
            );
            case PARTNER -> List.of(
                    entry("partnerOffice", "public.entry.partnerOffice", "public.entry.partnerOffice.description", "/partner-office", Audience.PARTNER),
                    entry("bonus", "public.entry.bonus", "public.entry.bonus.description", "/bonus-wallet", Audience.PARTNER)
            );
            default -> List.of();
        };
        return java.util.stream.Stream.concat(common.stream(), personal.stream()).toList();
    }

    @Override
    public NewsFeedResponse findNews(Audience audience) {
        NewsItemResponse featured = newsItem(
                "spring-collection",
                "brand-care-guide",
                "public.news.springCollection.title",
                "public.news.springCollection.summary",
                "public.news.category.campaign",
                "/assets/news/spring-collection.jpg",
                "2026-04-26T09:00:00Z"
        );
        return new NewsFeedResponse(
                List.of(featured, newsItem(
                        "partner-materials",
                        "partner-launch-guide",
                        "public.news.partnerMaterials.title",
                        "public.news.partnerMaterials.summary",
                        "public.news.category.partner",
                        "/assets/news/partner-materials.jpg",
                        "2026-04-26T10:00:00Z"
                )),
                featured,
                "STR_MNEMO_PUBLIC_NEWS_EMPTY"
        );
    }

    @Override
    public Optional<ContentPageResponse> findContentPage(String contentId, Audience audience) {
        if ("brand-care-guide".equals(contentId)) {
            return Optional.of(contentPage(audience));
        }
        if ("partner-launch-guide".equals(contentId)) {
            return Optional.of(partnerContentPage(audience));
        }
        return Optional.empty();
    }

    @Override
    public Optional<OfferResponse> findOffer(String offerId, Audience audience) {
        if (!"spring-offer".equals(offerId)) {
            return Optional.empty();
        }
        return Optional.of(new OfferResponse(
                "spring-offer",
                "public.offer.spring.title",
                "public.offer.spring.summary",
                List.of(
                        breadcrumb("public.navigation.home", "/"),
                        breadcrumb("public.offer.breadcrumb", "/offer/spring-offer")
                ),
                new SeoMetadataResponse(
                        "public.offer.spring.seo.title",
                        "public.offer.spring.seo.description",
                        "/offer/spring-offer"
                ),
                new OfferHeroResponse(
                        "public.offer.spring.title",
                        "public.offer.spring.summary",
                        "/assets/offers/spring-offer.jpg"
                ),
                List.of(new ContentSectionResponse("spring-conditions", "CONDITIONS", 10, Map.of(
                        "titleKey", "public.offer.spring.conditions.title",
                        "bodyKey", "public.offer.spring.conditions.body"
                ))),
                List.of(new ContentAttachmentResponse(
                        "spring-rules",
                        "PDF",
                        "public.offer.spring.rules.title",
                        "/assets/offers/spring-rules.pdf",
                        819200L
                )),
                List.of(new ProductLinkResponse("spring-campaign", "public.offer.spring.products", "/catalog")),
                ctasFor(audience, "public.offer.spring.cta", "/catalog")
        ));
    }

    @Override
    public FaqResponse findFaq(Audience audience, String category, String query) {
        List<FaqItemResponse> visibleItems = faqItems().stream()
                .filter(item -> matchesAudience(audience, item.audience()))
                .filter(item -> category == null || category.isBlank() || category.equals(item.categoryKey()))
                .filter(item -> query == null || query.isBlank() || matchesFaqQuery(item, query))
                .toList();
        List<FaqCategoryResponse> categories = List.of(
                faqCategory("all", "public.faq.category.all", faqItems().stream().filter(item -> matchesAudience(audience, item.audience())).toList().size()),
                faqCategory("orders", "public.faq.category.orders", countFaq(audience, "orders")),
                faqCategory("delivery", "public.faq.category.delivery", countFaq(audience, "delivery")),
                faqCategory("partner", "public.faq.category.partner", countFaq(audience, "partner")),
                faqCategory("returns", "public.faq.category.returns", countFaq(audience, "returns"))
        );
        return new FaqResponse(categories, visibleItems, "STR_MNEMO_PUBLIC_FAQ_EMPTY");
    }

    @Override
    public Optional<InfoSectionResponse> findInfoSection(String section, Audience audience) {
        if (section == null || section.isBlank() || "overview".equals(section)) {
            return Optional.of(infoOverview(audience));
        }
        if (!"delivery".equals(section)) {
            return Optional.empty();
        }
        return Optional.of(new InfoSectionResponse(
                "delivery",
                "public.info.delivery.title",
                "public.info.delivery.description",
                List.of(
                        breadcrumb("public.navigation.home", "/"),
                        breadcrumb("public.info.breadcrumb", "/info"),
                        breadcrumb("public.info.delivery.breadcrumb", "/info/delivery")
                ),
                new SeoMetadataResponse(
                        "public.info.delivery.seo.title",
                        "public.info.delivery.seo.description",
                        "/info/delivery"
                ),
                List.of(
                        new ContentSectionResponse("delivery-time", "RICH_TEXT", 10, Map.of(
                                "titleKey", "public.info.delivery.time.title",
                                "bodyKey", "public.info.delivery.time.body",
                                "anchor", "delivery-time"
                        )),
                        new ContentSectionResponse("pickup-points", "RICH_TEXT", 20, Map.of(
                                "titleKey", "public.info.delivery.pickup.title",
                                "bodyKey", "public.info.delivery.pickup.body",
                                "anchor", "pickup-points"
                        ))
                ),
                List.of(new InfoRelatedDocumentResponse(
                        "terms",
                        "public.documents.terms.title",
                        "/documents/terms"
                )),
                ctasFor(audience, "public.info.delivery.cta.documents", "/documents/terms")
        ));
    }

    @Override
    public Optional<DocumentCollectionResponse> findDocuments(String documentType, Audience audience) {
        List<DocumentResponse> documents = documents().stream()
                .filter(document -> documentType.equals(document.documentType()))
                .filter(document -> matchesAudience(audience, document.audience()))
                .toList();
        if (documents.isEmpty() && documents().stream().noneMatch(document -> documentType.equals(document.documentType()))) {
            return Optional.empty();
        }
        return Optional.of(new DocumentCollectionResponse(
                documentType,
                "public.documents." + documentType + ".title",
                "public.documents." + documentType + ".description",
                List.of(
                        breadcrumb("public.navigation.home", "/"),
                        breadcrumb("public.navigation.documents", "/documents/" + documentType)
                ),
                documents,
                "STR_MNEMO_PUBLIC_DOCUMENTS_EMPTY"
        ));
    }

    private static InfoSectionResponse infoOverview(Audience audience) {
        return new InfoSectionResponse(
                "overview",
                "public.info.overview.title",
                "public.info.overview.description",
                List.of(
                        breadcrumb("public.navigation.home", "/"),
                        breadcrumb("public.info.breadcrumb", "/info")
                ),
                new SeoMetadataResponse(
                        "public.info.overview.seo.title",
                        "public.info.overview.seo.description",
                        "/info"
                ),
                List.of(new ContentSectionResponse("overview-links", "LINK_LIST", 10, Map.of(
                        "titleKey", "public.info.overview.links.title",
                        "deliveryRoute", "/info/delivery"
                ))),
                List.of(new InfoRelatedDocumentResponse(
                        "terms",
                        "public.documents.terms.title",
                        "/documents/terms"
                )),
                ctasFor(audience, "public.info.overview.cta.faq", "/FAQ")
        );
    }

    private static List<FaqItemResponse> faqItems() {
        return List.of(
                new FaqItemResponse(
                        "delivery-time",
                        "delivery",
                        "public.faq.delivery.time.question",
                        "public.faq.delivery.time.answer",
                        List.of("delivery", "shipping", "timing"),
                        "delivery",
                        "terms",
                        Audience.ANY
                ),
                new FaqItemResponse(
                        "order-payment",
                        "orders",
                        "public.faq.orders.payment.question",
                        "public.faq.orders.payment.answer",
                        List.of("payment", "order", "checkout"),
                        null,
                        "terms",
                        Audience.ANY
                ),
                new FaqItemResponse(
                        "partner-documents",
                        "partner",
                        "public.faq.partner.documents.question",
                        "public.faq.partner.documents.answer",
                        List.of("partner", "documents", "agreement"),
                        "overview",
                        "partner",
                        Audience.PARTNER
                ),
                new FaqItemResponse(
                        "returns-claim",
                        "returns",
                        "public.faq.returns.claim.question",
                        "public.faq.returns.claim.answer",
                        List.of("return", "claim", "refund"),
                        "delivery",
                        "terms",
                        Audience.CUSTOMER
                )
        );
    }

    private static List<DocumentResponse> documents() {
        return List.of(
                new DocumentResponse(
                        "user-terms",
                        "terms",
                        "public.documents.userTerms.title",
                        "public.documents.userTerms.description",
                        "2.1",
                        "2026-04-26T00:00:00Z",
                        "/assets/documents/terms-v2-1.pdf",
                        "/assets/documents/terms-v2-1.pdf",
                        true,
                        true,
                        Audience.ANY,
                        List.of(new DocumentVersionResponse(
                                "2.0",
                                "2026-04-01T00:00:00Z",
                                "/assets/documents/terms-v2-0.pdf",
                                "/assets/documents/terms-v2-0.pdf",
                                false
                        ))
                ),
                new DocumentResponse(
                        "delivery-rules",
                        "terms",
                        "public.documents.deliveryRules.title",
                        "public.documents.deliveryRules.description",
                        "1.4",
                        "2026-04-26T00:00:00Z",
                        "/assets/documents/delivery-rules-v1-4.pdf",
                        "/assets/documents/delivery-rules-v1-4.pdf",
                        false,
                        true,
                        Audience.ANY,
                        List.of()
                ),
                new DocumentResponse(
                        "partner-public-guide",
                        "partner",
                        "public.documents.partnerPublicGuide.title",
                        "public.documents.partnerPublicGuide.description",
                        "1.0",
                        "2026-04-26T00:00:00Z",
                        "/assets/documents/partner-public-guide-v1-0.pdf",
                        "/assets/documents/partner-public-guide-v1-0.pdf",
                        false,
                        true,
                        Audience.ANY,
                        List.of()
                ),
                new DocumentResponse(
                        "partner-agreement",
                        "partner",
                        "public.documents.partnerAgreement.title",
                        "public.documents.partnerAgreement.description",
                        "3.2",
                        "2026-04-26T00:00:00Z",
                        "/assets/documents/partner-agreement-v3-2.pdf",
                        "/assets/documents/partner-agreement-v3-2.pdf",
                        true,
                        true,
                        Audience.PARTNER,
                        List.of(new DocumentVersionResponse(
                                "3.1",
                                "2026-04-01T00:00:00Z",
                                "/assets/documents/partner-agreement-v3-1.pdf",
                                "/assets/documents/partner-agreement-v3-1.pdf",
                                false
                        ))
                )
        );
    }

    private static boolean matchesFaqQuery(FaqItemResponse item, String query) {
        String normalized = query.toLowerCase(java.util.Locale.ROOT);
        return item.questionKey().toLowerCase(java.util.Locale.ROOT).contains(normalized)
                || item.answerKey().toLowerCase(java.util.Locale.ROOT).contains(normalized)
                || item.tags().stream().anyMatch(tag -> tag.toLowerCase(java.util.Locale.ROOT).contains(normalized));
    }

    private static int countFaq(Audience audience, String category) {
        return faqItems().stream()
                .filter(item -> matchesAudience(audience, item.audience()))
                .filter(item -> category.equals(item.categoryKey()))
                .toList()
                .size();
    }

    private static boolean matchesAudience(Audience requested, Audience itemAudience) {
        if (Audience.ANY.equals(itemAudience)) {
            return true;
        }
        if (Audience.AUTHENTICATED.equals(itemAudience)) {
            return !Audience.GUEST.equals(requested);
        }
        return itemAudience.equals(requested);
    }

    private static FaqCategoryResponse faqCategory(String categoryKey, String titleKey, int questionCount) {
        return new FaqCategoryResponse(categoryKey, titleKey, questionCount);
    }

    private static List<ContentBlockResponse> homeBlocks() {
        return List.of(
                new ContentBlockResponse("HOME_HERO", "HERO", 10, Map.of(
                        "titleKey", "public.home.hero.title",
                        "descriptionKey", "public.home.hero.description",
                        "primaryCtaRoute", "/catalog",
                        "secondaryCtaRoute", "/register"
                )),
                new ContentBlockResponse("HOME_PROMO_CURRENT", "PROMO", 20, Map.of(
                        "titleKey", "public.home.promo.current.title",
                        "targetRoute", "/catalog"
                )),
                new ContentBlockResponse("HOME_QUICK_LINKS", "QUICK_LINKS", 30, Map.of(
                        "titleKey", "public.home.quickLinks.title"
                ))
        );
    }

    private static List<ContentBlockResponse> communityBlocks() {
        return List.of(
                new ContentBlockResponse("COMMUNITY_OVERVIEW", "COMMUNITY_FEED", 10, Map.of(
                        "titleKey", "public.community.overview.title",
                        "ctaRoute", "/register"
                )),
                new ContentBlockResponse("COMMUNITY_FALLBACK", "FALLBACK", 20, Map.of(
                        "titleKey", "public.community.fallback.title",
                        "targetRoute", "/news"
                ))
        );
    }

    private static ContentPageResponse contentPage(Audience audience) {
        return new ContentPageResponse(
                "brand-care-guide",
                "GUIDE",
                "public.content.brandCareGuide.title",
                "public.content.brandCareGuide.description",
                List.of(
                        breadcrumb("public.navigation.home", "/"),
                        breadcrumb("public.navigation.news", "/news"),
                        breadcrumb("public.content.brandCareGuide.breadcrumb", "/content/brand-care-guide")
                ),
                new SeoMetadataResponse(
                        "public.content.brandCareGuide.seo.title",
                        "public.content.brandCareGuide.seo.description",
                        "/content/brand-care-guide"
                ),
                List.of(
                        new ContentSectionResponse("intro", "RICH_TEXT", 10, Map.of(
                                "titleKey", "public.content.brandCareGuide.intro.title",
                                "bodyKey", "public.content.brandCareGuide.intro.body"
                        )),
                        new ContentSectionResponse("routine-image", "IMAGE", 20, Map.of(
                                "imageUrl", "/assets/content/brand-care-guide.jpg",
                                "altKey", "public.content.brandCareGuide.image.alt"
                        ))
                ),
                List.of(new ContentAttachmentResponse(
                        "care-guide-pdf",
                        "PDF",
                        "public.content.brandCareGuide.pdf.title",
                        "/assets/content/brand-care-guide.pdf",
                        524288L
                )),
                List.of(new ProductLinkResponse("spring-campaign", "public.content.brandCareGuide.products", "/catalog")),
                ctasFor(audience, "public.content.brandCareGuide.cta.catalog", "/catalog")
        );
    }

    private static ContentPageResponse partnerContentPage(Audience audience) {
        return new ContentPageResponse(
                "partner-launch-guide",
                "ARTICLE",
                "public.content.partnerLaunchGuide.title",
                "public.content.partnerLaunchGuide.description",
                List.of(
                        breadcrumb("public.navigation.home", "/"),
                        breadcrumb("public.navigation.news", "/news"),
                        breadcrumb("public.content.partnerLaunchGuide.breadcrumb", "/content/partner-launch-guide")
                ),
                new SeoMetadataResponse(
                        "public.content.partnerLaunchGuide.seo.title",
                        "public.content.partnerLaunchGuide.seo.description",
                        "/content/partner-launch-guide"
                ),
                List.of(new ContentSectionResponse("partner-intro", "RICH_TEXT", 10, Map.of(
                        "titleKey", "public.content.partnerLaunchGuide.intro.title",
                        "bodyKey", "public.content.partnerLaunchGuide.intro.body"
                ))),
                List.of(),
                List.of(new ProductLinkResponse("partner-benefits", "public.content.partnerLaunchGuide.products", "/benefits")),
                ctasFor(audience, "public.content.partnerLaunchGuide.cta", "/register")
        );
    }

    private static NewsItemResponse newsItem(
            String newsKey,
            String contentId,
            String titleKey,
            String summaryKey,
            String categoryKey,
            String imageUrl,
            String publishedAt
    ) {
        return new NewsItemResponse(newsKey, contentId, titleKey, summaryKey, categoryKey, imageUrl, publishedAt, "/content/" + contentId);
    }

    private static BreadcrumbResponse breadcrumb(String labelKey, String route) {
        return new BreadcrumbResponse(labelKey, route);
    }

    private static List<ContentCtaResponse> ctasFor(Audience audience, String publicLabelKey, String publicTarget) {
        List<ContentCtaResponse> publicCtas = List.of(new ContentCtaResponse(
                publicLabelKey,
                "INTERNAL_ROUTE",
                publicTarget,
                Audience.ANY
        ));
        if (Audience.CUSTOMER.equals(audience)) {
            return java.util.stream.Stream.concat(publicCtas.stream(), java.util.stream.Stream.of(new ContentCtaResponse(
                    "public.content.cta.customerCart",
                    "INTERNAL_ROUTE",
                    "/cart",
                    Audience.CUSTOMER
            ))).toList();
        }
        return publicCtas;
    }

    private static NavigationItemResponse item(String key, String labelKey, String route, String area) {
        return new NavigationItemResponse(key, labelKey, "INTERNAL_ROUTE", route, area, List.of());
    }

    private static EntryPointResponse entry(String key, String labelKey, String descriptionKey, String route, Audience audience) {
        return new EntryPointResponse(key, labelKey, descriptionKey, route, audience);
    }
}
