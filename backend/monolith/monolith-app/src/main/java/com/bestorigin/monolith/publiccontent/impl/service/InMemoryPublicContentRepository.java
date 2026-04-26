package com.bestorigin.monolith.publiccontent.impl.service;

import com.bestorigin.monolith.publiccontent.api.Audience;
import com.bestorigin.monolith.publiccontent.api.ContentBlockResponse;
import com.bestorigin.monolith.publiccontent.api.EntryPointResponse;
import com.bestorigin.monolith.publiccontent.api.NavigationItemResponse;
import com.bestorigin.monolith.publiccontent.api.PublicPageResponse;
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
                item("documents", "public.navigation.documents", "/documents", "FOOTER"),
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

    private static NavigationItemResponse item(String key, String labelKey, String route, String area) {
        return new NavigationItemResponse(key, labelKey, "INTERNAL_ROUTE", route, area, List.of());
    }

    private static EntryPointResponse entry(String key, String labelKey, String descriptionKey, String route, Audience audience) {
        return new EntryPointResponse(key, labelKey, descriptionKey, route, audience);
    }
}
