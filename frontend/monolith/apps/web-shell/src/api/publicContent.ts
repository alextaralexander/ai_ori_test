export type Audience = 'GUEST' | 'CUSTOMER' | 'PARTNER';

export interface ContentBlock {
  blockKey: string;
  blockType: string;
  sortOrder: number;
  payload: Record<string, string>;
}

export interface NavigationItem {
  itemKey: string;
  labelKey: string;
  targetType: string;
  targetValue: string;
  area: string;
  children: NavigationItem[];
}

export interface EntryPoint {
  entryKey: string;
  labelKey: string;
  descriptionKey?: string;
  targetRoute: string;
  audience: string;
}

export interface PublicPage {
  pageKey: string;
  routePath: string;
  titleKey: string;
  blocks: ContentBlock[];
  navigation: NavigationItem[];
  entryPoints: EntryPoint[];
}

export async function loadPublicPage(page: 'home' | 'community', audience: Audience): Promise<PublicPage> {
  const response = await fetch(`/api/public-content/pages/${page}?audience=${audience}`, {
    headers: {
      Accept: 'application/json',
      'Accept-Language': navigator.language
    }
  });
  if (!response.ok) {
    return fallbackPage(page, audience);
  }
  return response.json() as Promise<PublicPage>;
}

function fallbackPage(page: 'home' | 'community', audience: Audience): PublicPage {
  const commonEntryPoints: EntryPoint[] = [
    entry('catalog', 'public.entry.catalog', 'public.entry.catalog.description', '/catalog', 'ANY'),
    entry('community', 'public.entry.community', 'public.entry.community.description', '/community', 'ANY'),
    entry('benefits', 'public.entry.benefits', 'public.entry.benefits.description', '/benefits', 'ANY'),
    entry('register', 'public.entry.register', 'public.entry.register.description', '/register', 'GUEST')
  ];
  const personalEntryPoints: EntryPoint[] = audience === 'CUSTOMER'
    ? [
        entry('profile', 'public.entry.profile', 'public.entry.profile.description', '/profile', 'CUSTOMER'),
        entry('cart', 'public.entry.cart', 'public.entry.cart.description', '/cart', 'CUSTOMER'),
        entry('orders', 'public.entry.orders', 'public.entry.orders.description', '/orders', 'CUSTOMER')
      ]
    : audience === 'PARTNER'
      ? [
          entry('partnerOffice', 'public.entry.partnerOffice', 'public.entry.partnerOffice.description', '/partner-office', 'PARTNER'),
          entry('bonus', 'public.entry.bonus', 'public.entry.bonus.description', '/bonus-wallet', 'PARTNER')
        ]
      : [];

  return {
    pageKey: page === 'community' ? 'COMMUNITY' : 'HOME',
    routePath: page === 'community' ? '/community' : '/home',
    titleKey: page === 'community' ? 'public.community.title' : 'public.home.title',
    blocks: page === 'community'
      ? [
          { blockKey: 'COMMUNITY_OVERVIEW', blockType: 'COMMUNITY_FEED', sortOrder: 10, payload: { titleKey: 'public.community.overview.title' } },
          { blockKey: 'COMMUNITY_FALLBACK', blockType: 'FALLBACK', sortOrder: 20, payload: { titleKey: 'public.community.fallback.title' } }
        ]
      : [
          { blockKey: 'HOME_HERO', blockType: 'HERO', sortOrder: 10, payload: { titleKey: 'public.home.hero.title', descriptionKey: 'public.home.hero.description' } },
          { blockKey: 'HOME_PROMO_CURRENT', blockType: 'PROMO', sortOrder: 20, payload: { titleKey: 'public.home.promo.current.title' } }
        ],
    navigation: [
      nav('catalog', 'public.navigation.catalog', '/catalog', 'HEADER'),
      nav('community', 'public.navigation.community', '/community', 'HEADER'),
      nav('benefits', 'public.navigation.benefits', '/benefits', 'HEADER'),
      nav('register', 'public.navigation.register', '/register', 'HEADER'),
      nav('cart', 'public.navigation.cart', '/cart', 'HEADER'),
      nav('documents', 'public.navigation.documents', '/documents', 'FOOTER'),
      nav('contacts', 'public.navigation.contacts', '/contacts', 'FOOTER')
    ],
    entryPoints: [...commonEntryPoints, ...personalEntryPoints]
  };
}

function entry(entryKey: string, labelKey: string, descriptionKey: string, targetRoute: string, audience: string): EntryPoint {
  return { entryKey, labelKey, descriptionKey, targetRoute, audience };
}

function nav(itemKey: string, labelKey: string, targetValue: string, area: string): NavigationItem {
  return { itemKey, labelKey, targetType: 'INTERNAL_ROUTE', targetValue, area, children: [] };
}
