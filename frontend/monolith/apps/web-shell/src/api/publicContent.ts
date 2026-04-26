export type Audience = 'GUEST' | 'CUSTOMER' | 'PARTNER' | 'CONTENT_MANAGER' | 'CATALOG_MANAGER';

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

export interface NewsItem {
  newsKey: string;
  contentId: string;
  titleKey: string;
  summaryKey?: string;
  categoryKey?: string;
  imageUrl?: string;
  publishedAt: string;
  targetRoute: string;
}

export interface NewsFeed {
  items: NewsItem[];
  featured: NewsItem | null;
  emptyStateCode: string;
}

export interface Breadcrumb {
  labelKey: string;
  route: string;
}

export interface SeoMetadata {
  titleKey: string;
  descriptionKey: string;
  canonicalUrl?: string;
}

export interface ContentSection {
  sectionKey: string;
  sectionType: string;
  sortOrder: number;
  payload: Record<string, string>;
}

export interface ContentAttachment {
  attachmentKey: string;
  fileType: string;
  titleKey: string;
  url: string;
  fileSizeBytes?: number;
}

export interface ProductLink {
  productRef: string;
  labelKey?: string;
  targetRoute: string;
}

export interface ContentCta {
  labelKey: string;
  targetType: string;
  targetValue: string;
  audience: Audience | 'ANY';
}

export interface OfferHero {
  titleKey: string;
  summaryKey?: string;
  imageUrl?: string;
}

export interface ContentPage {
  contentId: string;
  templateCode: string;
  titleKey: string;
  descriptionKey?: string;
  breadcrumbs: Breadcrumb[];
  seo: SeoMetadata;
  sections: ContentSection[];
  attachments: ContentAttachment[];
  productLinks: ProductLink[];
  ctas: ContentCta[];
}

export interface OfferPage {
  offerId: string;
  titleKey: string;
  summaryKey?: string;
  breadcrumbs: Breadcrumb[];
  seo: SeoMetadata;
  hero: OfferHero;
  sections: ContentSection[];
  attachments: ContentAttachment[];
  productLinks: ProductLink[];
  ctas: ContentCta[];
}

export interface FaqCategory {
  categoryKey: string;
  titleKey: string;
  questionCount: number;
}

export interface FaqItem {
  itemKey: string;
  categoryKey: string;
  questionKey: string;
  answerKey: string;
  tags: string[];
  relatedInfoSection?: string;
  relatedDocumentType?: string;
  audience: Audience | 'ANY';
}

export interface FaqPage {
  categories: FaqCategory[];
  items: FaqItem[];
  emptyStateCode: string;
}

export interface InfoRelatedDocument {
  documentType: string;
  titleKey: string;
  targetRoute: string;
}

export interface InfoPage {
  sectionCode: string;
  titleKey: string;
  descriptionKey?: string;
  breadcrumbs: Breadcrumb[];
  seo: SeoMetadata;
  sections: ContentSection[];
  documents: InfoRelatedDocument[];
  ctas: ContentCta[];
}

export interface DocumentVersion {
  versionLabel: string;
  publishedAt: string;
  viewerUrl: string;
  downloadUrl: string;
  current: boolean;
}

export interface PublicDocument {
  documentKey: string;
  documentType: string;
  titleKey: string;
  descriptionKey?: string;
  versionLabel: string;
  publishedAt: string;
  viewerUrl: string;
  downloadUrl: string;
  required: boolean;
  current: boolean;
  audience: Audience | 'ANY';
  archive: DocumentVersion[];
}

export interface DocumentCollection {
  documentType: string;
  titleKey: string;
  descriptionKey?: string;
  breadcrumbs: Breadcrumb[];
  documents: PublicDocument[];
  emptyStateCode: string;
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
      nav('documents', 'public.navigation.documents', '/documents/terms', 'FOOTER'),
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

export async function loadNews(audience: Audience): Promise<NewsFeed> {
  const response = await fetch(`/api/public-content/news?audience=${audience}`, requestOptions());
  if (!response.ok) {
    return fallbackNews();
  }
  return response.json() as Promise<NewsFeed>;
}

export async function loadContentPage(contentId: string, audience: Audience): Promise<ContentPage | null> {
  const response = await fetch(`/api/public-content/content/${contentId}?audience=${audience}`, requestOptions());
  if (response.status === 404) {
    return null;
  }
  if (!response.ok) {
    return contentId === 'brand-care-guide' ? fallbackContentPage(audience) : null;
  }
  return response.json() as Promise<ContentPage>;
}

export async function loadOffer(offerId: string, audience: Audience): Promise<OfferPage | null> {
  const response = await fetch(`/api/public-content/offers/${offerId}?audience=${audience}`, requestOptions());
  if (response.status === 404) {
    return null;
  }
  if (!response.ok) {
    return offerId === 'spring-offer' ? fallbackOffer(audience) : null;
  }
  return response.json() as Promise<OfferPage>;
}

export async function loadFaq(audience: Audience, query = '', category = ''): Promise<FaqPage> {
  const params = new URLSearchParams({ audience });
  if (query) {
    params.set('query', query);
  }
  if (category) {
    params.set('category', category);
  }
  const response = await fetch(`/api/public-content/faq?${params.toString()}`, requestOptions());
  if (!response.ok) {
    return fallbackFaq(audience, query, category);
  }
  return response.json() as Promise<FaqPage>;
}

export async function loadInfoSection(section: string, audience: Audience): Promise<InfoPage | null> {
  const target = section || 'overview';
  const response = await fetch(`/api/public-content/info/${target}?audience=${audience}`, requestOptions());
  if (response.status === 404) {
    return null;
  }
  if (!response.ok) {
    return target === 'delivery' || target === 'overview' ? fallbackInfo(target, audience) : null;
  }
  return response.json() as Promise<InfoPage>;
}

export async function loadDocuments(documentType: string, audience: Audience): Promise<DocumentCollection | null> {
  const response = await fetch(`/api/public-content/documents/${documentType}?audience=${audience}`, requestOptions());
  if (response.status === 404) {
    return null;
  }
  if (!response.ok) {
    return documentType === 'terms' || documentType === 'partner' ? fallbackDocuments(documentType, audience) : null;
  }
  return response.json() as Promise<DocumentCollection>;
}

function requestOptions(): RequestInit {
  return {
    headers: {
      Accept: 'application/json',
      'Accept-Language': navigator.language
    }
  };
}

function fallbackNews(): NewsFeed {
  const featured = {
    newsKey: 'spring-collection',
    contentId: 'brand-care-guide',
    titleKey: 'public.news.springCollection.title',
    summaryKey: 'public.news.springCollection.summary',
    categoryKey: 'public.news.category.campaign',
    imageUrl: '/assets/news/spring-collection.jpg',
    publishedAt: '2026-04-26T09:00:00Z',
    targetRoute: '/content/brand-care-guide'
  };
  return {
    items: [
      featured,
      {
        newsKey: 'partner-materials',
        contentId: 'partner-launch-guide',
        titleKey: 'public.news.partnerMaterials.title',
        summaryKey: 'public.news.partnerMaterials.summary',
        categoryKey: 'public.news.category.partner',
        imageUrl: '/assets/news/partner-materials.jpg',
        publishedAt: '2026-04-26T10:00:00Z',
        targetRoute: '/content/partner-launch-guide'
      }
    ],
    featured,
    emptyStateCode: 'STR_MNEMO_PUBLIC_NEWS_EMPTY'
  };
}

function fallbackContentPage(audience: Audience): ContentPage {
  return {
    contentId: 'brand-care-guide',
    templateCode: 'GUIDE',
    titleKey: 'public.content.brandCareGuide.title',
    descriptionKey: 'public.content.brandCareGuide.description',
    breadcrumbs: breadcrumbs('public.content.brandCareGuide.breadcrumb', '/content/brand-care-guide'),
    seo: {
      titleKey: 'public.content.brandCareGuide.seo.title',
      descriptionKey: 'public.content.brandCareGuide.seo.description',
      canonicalUrl: '/content/brand-care-guide'
    },
    sections: [
      {
        sectionKey: 'intro',
        sectionType: 'RICH_TEXT',
        sortOrder: 10,
        payload: {
          titleKey: 'public.content.brandCareGuide.intro.title',
          bodyKey: 'public.content.brandCareGuide.intro.body'
        }
      }
    ],
    attachments: [
      {
        attachmentKey: 'care-guide-pdf',
        fileType: 'PDF',
        titleKey: 'public.content.brandCareGuide.pdf.title',
        url: '/assets/content/brand-care-guide.pdf',
        fileSizeBytes: 524288
      }
    ],
    productLinks: [{ productRef: 'spring-campaign', labelKey: 'public.content.brandCareGuide.products', targetRoute: '/catalog' }],
    ctas: ctasFor(audience, 'public.content.brandCareGuide.cta.catalog', '/catalog')
  };
}

function fallbackOffer(audience: Audience): OfferPage {
  return {
    offerId: 'spring-offer',
    titleKey: 'public.offer.spring.title',
    summaryKey: 'public.offer.spring.summary',
    breadcrumbs: [
      { labelKey: 'public.navigation.home', route: '/' },
      { labelKey: 'public.offer.breadcrumb', route: '/offer/spring-offer' }
    ],
    seo: {
      titleKey: 'public.offer.spring.seo.title',
      descriptionKey: 'public.offer.spring.seo.description',
      canonicalUrl: '/offer/spring-offer'
    },
    hero: {
      titleKey: 'public.offer.spring.title',
      summaryKey: 'public.offer.spring.summary',
      imageUrl: '/assets/offers/spring-offer.jpg'
    },
    sections: [
      {
        sectionKey: 'spring-conditions',
        sectionType: 'CONDITIONS',
        sortOrder: 10,
        payload: {
          titleKey: 'public.offer.spring.conditions.title',
          bodyKey: 'public.offer.spring.conditions.body'
        }
      }
    ],
    attachments: [
      {
        attachmentKey: 'spring-rules',
        fileType: 'PDF',
        titleKey: 'public.offer.spring.rules.title',
        url: '/assets/offers/spring-rules.pdf',
        fileSizeBytes: 819200
      }
    ],
    productLinks: [{ productRef: 'spring-campaign', labelKey: 'public.offer.spring.products', targetRoute: '/catalog' }],
    ctas: ctasFor(audience, 'public.offer.spring.cta', '/catalog')
  };
}

function fallbackFaq(audience: Audience, query: string, category: string): FaqPage {
  const items: FaqItem[] = [
    {
      itemKey: 'delivery-time',
      categoryKey: 'delivery',
      questionKey: 'public.faq.delivery.time.question',
      answerKey: 'public.faq.delivery.time.answer',
      tags: ['delivery', 'shipping', 'доставка'],
      relatedInfoSection: 'delivery',
      relatedDocumentType: 'terms',
      audience: 'ANY'
    },
    {
      itemKey: 'partner-documents',
      categoryKey: 'partner',
      questionKey: 'public.faq.partner.documents.question',
      answerKey: 'public.faq.partner.documents.answer',
      tags: ['partner', 'documents'],
      relatedInfoSection: 'overview',
      relatedDocumentType: 'partner',
      audience: 'PARTNER'
    }
  ];
  const filtered = items
    .filter((item) => item.audience === 'ANY' || item.audience === audience)
    .filter((item) => !category || item.categoryKey === category)
    .filter((item) => !query || item.tags.some((tag) => tag.toLowerCase().includes(query.toLowerCase())));
  return {
    categories: [
      { categoryKey: 'all', titleKey: 'public.faq.category.all', questionCount: filtered.length },
      { categoryKey: 'delivery', titleKey: 'public.faq.category.delivery', questionCount: filtered.filter((item) => item.categoryKey === 'delivery').length },
      { categoryKey: 'partner', titleKey: 'public.faq.category.partner', questionCount: filtered.filter((item) => item.categoryKey === 'partner').length }
    ],
    items: filtered,
    emptyStateCode: 'STR_MNEMO_PUBLIC_FAQ_EMPTY'
  };
}

function fallbackInfo(section: string, audience: Audience): InfoPage {
  return {
    sectionCode: section || 'overview',
    titleKey: section === 'delivery' ? 'public.info.delivery.title' : 'public.info.overview.title',
    descriptionKey: section === 'delivery' ? 'public.info.delivery.description' : 'public.info.overview.description',
    breadcrumbs: [
      { labelKey: 'public.navigation.home', route: '/' },
      { labelKey: 'public.info.breadcrumb', route: '/info' }
    ],
    seo: {
      titleKey: section === 'delivery' ? 'public.info.delivery.seo.title' : 'public.info.overview.seo.title',
      descriptionKey: section === 'delivery' ? 'public.info.delivery.seo.description' : 'public.info.overview.seo.description',
      canonicalUrl: section === 'delivery' ? '/info/delivery' : '/info'
    },
    sections: [
      {
        sectionKey: 'delivery-time',
        sectionType: 'RICH_TEXT',
        sortOrder: 10,
        payload: {
          titleKey: 'public.info.delivery.time.title',
          bodyKey: 'public.info.delivery.time.body',
          anchor: 'delivery-time'
        }
      }
    ],
    documents: [{ documentType: 'terms', titleKey: 'public.documents.terms.title', targetRoute: '/documents/terms' }],
    ctas: ctasFor(audience, 'public.info.delivery.cta.documents', '/documents/terms')
  };
}

function fallbackDocuments(documentType: string, audience: Audience): DocumentCollection {
  const terms: PublicDocument = {
    documentKey: 'user-terms',
    documentType: 'terms',
    titleKey: 'public.documents.userTerms.title',
    descriptionKey: 'public.documents.userTerms.description',
    versionLabel: '2.1',
    publishedAt: '2026-04-26T00:00:00Z',
    viewerUrl: '/assets/documents/terms-v2-1.pdf',
    downloadUrl: '/assets/documents/terms-v2-1.pdf',
    required: true,
    current: true,
    audience: 'ANY',
    archive: [{ versionLabel: '2.0', publishedAt: '2026-04-01T00:00:00Z', viewerUrl: '/assets/documents/terms-v2-0.pdf', downloadUrl: '/assets/documents/terms-v2-0.pdf', current: false }]
  };
  const partner: PublicDocument = {
    documentKey: 'partner-agreement',
    documentType: 'partner',
    titleKey: 'public.documents.partnerAgreement.title',
    descriptionKey: 'public.documents.partnerAgreement.description',
    versionLabel: '3.2',
    publishedAt: '2026-04-26T00:00:00Z',
    viewerUrl: '/assets/documents/partner-agreement-v3-2.pdf',
    downloadUrl: '/assets/documents/partner-agreement-v3-2.pdf',
    required: true,
    current: true,
    audience: 'PARTNER',
    archive: []
  };
  const documents = documentType === 'partner' && audience === 'PARTNER' ? [terms, partner] : [terms];
  return {
    documentType,
    titleKey: `public.documents.${documentType}.title`,
    descriptionKey: `public.documents.${documentType}.description`,
    breadcrumbs: [
      { labelKey: 'public.navigation.home', route: '/' },
      { labelKey: 'public.navigation.documents', route: `/documents/${documentType}` }
    ],
    documents,
    emptyStateCode: 'STR_MNEMO_PUBLIC_DOCUMENTS_EMPTY'
  };
}

function breadcrumbs(currentLabelKey: string, route: string): Breadcrumb[] {
  return [
    { labelKey: 'public.navigation.home', route: '/' },
    { labelKey: 'public.navigation.news', route: '/news' },
    { labelKey: currentLabelKey, route }
  ];
}

function ctasFor(audience: Audience, labelKey: string, targetValue: string): ContentCta[] {
  const ctas: ContentCta[] = [{ labelKey, targetType: 'INTERNAL_ROUTE', targetValue, audience: 'ANY' }];
  if (audience === 'CUSTOMER') {
    ctas.push({ labelKey: 'public.content.cta.customerCart', targetType: 'INTERNAL_ROUTE', targetValue: '/cart', audience: 'CUSTOMER' });
  }
  return ctas;
}
