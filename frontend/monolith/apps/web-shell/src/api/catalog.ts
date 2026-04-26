import type { Audience } from './publicContent';
import { t } from '../i18n';

export interface CatalogProductCard {
  id: string;
  sku: string;
  productCode?: string;
  slug: string;
  nameKey: string;
  descriptionKey: string;
  categorySlug: string;
  categoryName?: string;
  imageUrl: string;
  name?: string;
  brand?: string;
  volumeLabel?: string;
  campaignCode?: string;
  price: number;
  promoPrice?: number | null;
  currency: string;
  availability: 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';
  orderLimits?: CatalogOrderLimits;
  media?: CatalogProductMedia[];
  information?: CatalogProductInformation;
  attachments?: CatalogProductAttachment[];
  recommendations?: CatalogProductRecommendation[];
  tags: string[];
  promoBadges: string[];
  canAddToCart: boolean;
  unavailableReasonCode?: string | null;
}

export interface CatalogOrderLimits {
  minQuantity: number;
  maxQuantity: number;
}

export interface CatalogProductMedia {
  url: string;
  altText: string;
  primary: boolean;
  sortOrder: number;
}

export interface CatalogProductInformation {
  shortDescription?: string;
  fullDescription?: string;
  usageInstructions?: string;
  ingredients?: string;
  characteristics?: CatalogCharacteristic[];
}

export interface CatalogCharacteristic {
  name: string;
  value: string;
}

export interface CatalogProductAttachment {
  title: string;
  documentType: string;
  url: string;
}

export interface CatalogProductRecommendation {
  productCode: string;
  name: string;
  imageUrl: string;
  price: number;
  currency: string;
  availability: CatalogProductCard['availability'];
  recommendationType: 'RELATED' | 'CROSS_SELL' | 'ALTERNATIVE';
}

export interface CatalogSearchResponse {
  items: CatalogProductCard[];
  recommendations: CatalogProductCard[];
  page: number;
  pageSize: number;
  totalItems: number;
  hasNextPage: boolean;
  messageCode?: string | null;
}

export interface CartSummaryResponse {
  itemsCount: number;
  totalQuantity: number;
  messageCode: string;
  partnerContext?: boolean;
}

export interface DigitalCatalogueIssue {
  issueCode: string;
  title: string;
  periodType: 'CURRENT' | 'NEXT' | 'ARCHIVE';
  period: { startDate: string; endDate: string };
  publicationStatus: 'DRAFT' | 'SCHEDULED' | 'PUBLISHED' | 'ARCHIVED';
  viewer: { zoom: boolean; download: boolean; share: boolean };
  pages: DigitalCataloguePage[];
  materials: DigitalCatalogueMaterial[];
}

export interface DigitalCataloguePage {
  pageNumber: number;
  imageUrl: string;
  thumbnailUrl?: string | null;
  widthPx?: number;
  heightPx?: number;
  hotspots: DigitalCatalogueHotspot[];
}

export interface DigitalCatalogueHotspot {
  productCode: string;
  xPercent: number;
  yPercent: number;
  widthPercent: number;
  heightPercent: number;
}

export interface DigitalCatalogueMaterial {
  materialId: string;
  materialType: 'MAIN_CATALOG' | 'BROCHURE' | 'PROMO_LEAFLET' | 'DOCUMENT';
  title: string;
  fileSizeBytes?: number | null;
  publicationStatus: string;
  previewUrl?: string | null;
  actions: { canOpen: boolean; canDownload: boolean; canShare: boolean };
}

export interface DigitalCatalogueMaterialActionResponse {
  url: string;
  expiresAt: string;
  messageCode: string;
}

export async function loadCatalogSearch(params: URLSearchParams, audience: Audience): Promise<CatalogSearchResponse> {
  const requestParams = new URLSearchParams(params);
  requestParams.set('audience', audience);
  const response = await fetch(`/api/catalog/search?${requestParams.toString()}`, requestOptions());
  if (!response.ok) {
    return fallbackCatalogSearch(requestParams);
  }
  return response.json() as Promise<CatalogSearchResponse>;
}

export async function loadCatalogProductCard(productCode: string, audience: Audience): Promise<CatalogProductCard | null> {
  const response = await fetch(`/api/catalog/products/${encodeURIComponent(productCode)}?audience=${audience}`, requestOptions());
  if (response.status === 404) {
    return null;
  }
  if (!response.ok) {
    return fallbackProductByCode(productCode);
  }
  return response.json() as Promise<CatalogProductCard>;
}

export async function addCatalogItemToCart(
  productId: string | null,
  audience: Audience,
  searchUrl: string,
  quantity = 1,
  source: 'SEARCH_RESULT' | 'PRODUCT_CARD' = 'SEARCH_RESULT',
  productCode?: string
): Promise<CartSummaryResponse> {
  const response = await fetch('/api/catalog/cart/items', {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      'Accept-Language': navigator.language
    },
    body: JSON.stringify({
      productId,
      productCode,
      quantity,
      audience,
      userContextId: getUserContextId(audience),
      searchUrl,
      partnerContextId: audience === 'PARTNER' ? getUserContextId(audience) : null,
      source
    })
  });
  if (!response.ok) {
    const error = await response.json().catch(() => ({ messageCode: 'STR_MNEMO_CATALOG_ITEM_UNAVAILABLE' }));
    return { itemsCount: 0, totalQuantity: 0, messageCode: error.messageCode ?? 'STR_MNEMO_CATALOG_ITEM_UNAVAILABLE' };
  }
  return response.json() as Promise<CartSummaryResponse>;
}

export async function loadDigitalCatalogue(kind: 'current' | 'next', audience: Audience, preview?: boolean): Promise<DigitalCatalogueIssue | { messageCode: string }> {
  const params = new URLSearchParams({ audience });
  if (preview !== undefined) {
    params.set('preview', String(preview));
  }
  const response = await fetch(`/api/catalog/digital-catalogues/${kind}?${params.toString()}`, requestOptions());
  if (!response.ok) {
    if (kind === 'next' && audience === 'GUEST' && preview === false) {
      return { messageCode: 'STR_MNEMO_DIGITAL_CATALOGUE_FORBIDDEN' };
    }
    return fallbackDigitalCatalogue(kind, audience, preview);
  }
  return response.json() as Promise<DigitalCatalogueIssue>;
}

export async function runDigitalCatalogueMaterialAction(materialId: string, action: 'download' | 'share', audience: Audience): Promise<DigitalCatalogueMaterialActionResponse> {
  const response = await fetch(`/api/catalog/digital-catalogues/materials/${encodeURIComponent(materialId)}/${action}`, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      'Accept-Language': navigator.language
    },
    body: JSON.stringify({ audience, userContextId: getUserContextId(audience), returnUrl: window.location.pathname })
  });
  if (!response.ok) {
    return {
      url: '',
      expiresAt: new Date().toISOString(),
      messageCode: action === 'download' ? 'STR_MNEMO_DIGITAL_CATALOGUE_DOWNLOAD_NOT_ALLOWED' : 'STR_MNEMO_DIGITAL_CATALOGUE_SHARE_NOT_ALLOWED'
    };
  }
  return response.json() as Promise<DigitalCatalogueMaterialActionResponse>;
}

function getUserContextId(audience: Audience): string {
  const key = `bestorigin.${audience.toLowerCase()}.cartContext`;
  const existing = window.sessionStorage.getItem(key);
  if (existing) {
    return existing;
  }
  const created = `${audience.toLowerCase()}-web-session-${Date.now()}-${Math.random().toString(16).slice(2)}`;
  window.sessionStorage.setItem(key, created);
  return created;
}

function requestOptions(): RequestInit {
  return {
    headers: {
      Accept: 'application/json',
      'Accept-Language': navigator.language
    }
  };
}

function fallbackCatalogSearch(params: URLSearchParams): CatalogSearchResponse {
  const query = (params.get('q') ?? '').toLowerCase();
  const category = params.get('category') ?? '';
  const availability = params.get('availability') ?? 'all';
  const promo = params.get('promo') === 'true';
  const products = fallbackProducts().filter((product) => {
    const queryMatch = !query || product.sku.toLowerCase().includes(query) || product.slug.includes(query) || product.tags.some((tag) => tag.includes(query));
    const categoryMatch = !category || product.categorySlug === category;
    const availabilityMatch = availability === 'all' || !availability || (availability === 'inStock' ? product.availability !== 'OUT_OF_STOCK' : product.availability === 'OUT_OF_STOCK');
    const promoMatch = !promo || product.promoBadges.length > 0;
    return queryMatch && categoryMatch && availabilityMatch && promoMatch;
  });
  const recommendations = fallbackProducts().filter((product) => product.availability !== 'OUT_OF_STOCK').slice(0, 4);
  return {
    items: products,
    recommendations,
    page: 0,
    pageSize: 12,
    totalItems: products.length,
    hasNextPage: false,
    messageCode: products.length === 0 ? 'STR_MNEMO_CATALOG_SEARCH_EMPTY' : null
  };
}

function fallbackProducts(): CatalogProductCard[] {
  return [
    {
      id: '11111111-1111-1111-1111-111111111111',
      sku: 'BOG-CREAM-001',
      productCode: 'BOG-CREAM-001',
      slug: 'hydrating-face-cream',
      nameKey: 'catalog.product.hydratingFaceCream.name',
      descriptionKey: 'catalog.product.hydratingFaceCream.description',
      categorySlug: 'face-care',
      categoryName: 'Уход за лицом',
      imageUrl: '/assets/catalog/hydrating-face-cream.jpg',
      name: 'Увлажняющий крем Best Ori Gin',
      brand: 'Best Ori Gin',
      volumeLabel: '50 ml',
      campaignCode: '2026-C07',
      price: 1290,
      promoPrice: 990,
      currency: 'RUB',
      availability: 'IN_STOCK',
      orderLimits: { minQuantity: 1, maxQuantity: 12 },
      media: [
        { url: '/assets/catalog/hydrating-face-cream.jpg', altText: 'Увлажняющий крем Best Ori Gin', primary: true, sortOrder: 1 },
        { url: '/assets/catalog/hydrating-face-cream-detail.jpg', altText: 'Текстура крема Best Ori Gin', primary: false, sortOrder: 2 }
      ],
      information: productInformation('Увлажняющий крем Best Ori Gin', '50 ml'),
      attachments: [{ title: 'Инструкция и состав', documentType: 'PRODUCT_INFO', url: '/assets/catalog/hydrating-face-cream-info.pdf' }],
      recommendations: [],
      tags: ['cream', 'face-care', 'hydration', 'partner'],
      promoBadges: ['new', 'campaign-hit'],
      canAddToCart: true
    },
    {
      id: '22222222-2222-2222-2222-222222222222',
      sku: 'BOG-SERUM-002',
      productCode: 'BOG-SERUM-002',
      slug: 'vitamin-glow-serum',
      nameKey: 'catalog.product.vitaminGlowSerum.name',
      descriptionKey: 'catalog.product.vitaminGlowSerum.description',
      categorySlug: 'face-care',
      categoryName: 'Уход за лицом',
      imageUrl: '/assets/catalog/vitamin-glow-serum.jpg',
      name: 'Сыворотка Vitamin Glow',
      brand: 'Best Ori Gin',
      volumeLabel: '30 ml',
      campaignCode: '2026-C07',
      price: 1590,
      promoPrice: 1390,
      currency: 'RUB',
      availability: 'LOW_STOCK',
      orderLimits: { minQuantity: 1, maxQuantity: 6 },
      media: [{ url: '/assets/catalog/vitamin-glow-serum.jpg', altText: 'Сыворотка Vitamin Glow', primary: true, sortOrder: 1 }],
      information: productInformation('Сыворотка Vitamin Glow', '30 ml'),
      attachments: [],
      recommendations: [],
      tags: ['serum', 'face-care', 'glow'],
      promoBadges: ['campaign-hit'],
      canAddToCart: true
    },
    {
      id: '33333333-3333-3333-3333-333333333333',
      sku: 'BOG-SOLDOUT-001',
      productCode: 'BOG-SOLDOUT-001',
      slug: 'velvet-lipstick',
      nameKey: 'catalog.product.velvetLipstick.name',
      descriptionKey: 'catalog.product.velvetLipstick.description',
      categorySlug: 'makeup',
      categoryName: 'Макияж',
      imageUrl: '/assets/catalog/velvet-lipstick.jpg',
      name: 'Бархатная помада',
      brand: 'Best Ori Gin',
      volumeLabel: '4 g',
      campaignCode: '2026-C07',
      price: 790,
      promoPrice: null,
      currency: 'RUB',
      availability: 'OUT_OF_STOCK',
      orderLimits: { minQuantity: 1, maxQuantity: 5 },
      media: [{ url: '/assets/catalog/velvet-lipstick.jpg', altText: 'Бархатная помада', primary: true, sortOrder: 1 }],
      information: productInformation('Бархатная помада', '4 g'),
      attachments: [],
      recommendations: [],
      tags: ['makeup', 'lipstick'],
      promoBadges: ['limited'],
      canAddToCart: false,
      unavailableReasonCode: 'STR_MNEMO_CATALOG_ITEM_UNAVAILABLE'
    }
  ];
}

function fallbackProductByCode(productCode: string): CatalogProductCard | null {
  const products = fallbackProducts();
  const product = products.find((item) => item.sku === productCode || item.productCode === productCode) ?? null;
  if (!product) {
    return null;
  }
  return {
    ...product,
    recommendations: products
      .filter((item) => item.sku !== product.sku && item.availability !== 'OUT_OF_STOCK')
      .map((item) => ({
        productCode: item.sku,
        name: item.name ?? item.sku,
        imageUrl: item.imageUrl,
        price: item.price,
        currency: item.currency,
        availability: item.availability,
        recommendationType: item.tags.includes('serum') ? 'CROSS_SELL' : 'RELATED'
      }))
  };
}

function fallbackDigitalCatalogue(kind: 'current' | 'next', audience: Audience, preview?: boolean): DigitalCatalogueIssue | { messageCode: string } {
  if (kind === 'next' && audience === 'GUEST' && preview === false) {
    return { messageCode: 'STR_MNEMO_DIGITAL_CATALOGUE_FORBIDDEN' };
  }
  const issueCode = kind === 'current' ? 'catalog-2026-05' : 'catalog-2026-06';
  const title = t(kind === 'current' ? 'catalog.digital.fallback.currentTitle' : 'catalog.digital.fallback.nextTitle');
  return {
    issueCode,
    title,
    periodType: kind === 'current' ? 'CURRENT' : 'NEXT',
    period: kind === 'current'
      ? { startDate: '2026-04-27', endDate: '2026-05-17' }
      : { startDate: '2026-05-18', endDate: '2026-06-07' },
    publicationStatus: kind === 'current' ? 'PUBLISHED' : 'PUBLISHED',
    viewer: { zoom: true, download: true, share: true },
    pages: [
      {
        pageNumber: 1,
        imageUrl: `/assets/catalogues/${issueCode}/page-1.jpg`,
        thumbnailUrl: `/assets/catalogues/${issueCode}/thumb-1.jpg`,
        widthPx: 1240,
        heightPx: 1754,
        hotspots: [{ productCode: 'BOG-CREAM-001', xPercent: 22.5, yPercent: 36, widthPercent: 18, heightPercent: 12 }]
      },
      {
        pageNumber: 2,
        imageUrl: `/assets/catalogues/${issueCode}/page-2.jpg`,
        thumbnailUrl: `/assets/catalogues/${issueCode}/thumb-2.jpg`,
        widthPx: 1240,
        heightPx: 1754,
        hotspots: [{ productCode: 'BOG-CREAM-001', xPercent: 22.5, yPercent: 36, widthPercent: 18, heightPercent: 12 }]
      }
    ],
    materials: [
      {
        materialId: 'catalog-current-pdf',
        materialType: 'MAIN_CATALOG',
        title: t('catalog.digital.fallback.mainPdfTitle'),
        fileSizeBytes: 4200000,
        publicationStatus: 'PUBLISHED',
        previewUrl: `/assets/catalogues/${issueCode}/catalog.pdf`,
        actions: { canOpen: true, canDownload: true, canShare: true }
      },
      {
        materialId: `${issueCode}-brochure`,
        materialType: 'BROCHURE',
        title: t('catalog.digital.fallback.brochureTitle'),
        fileSizeBytes: 920000,
        publicationStatus: 'PUBLISHED',
        previewUrl: `/assets/catalogues/${issueCode}/brochure.pdf`,
        actions: { canOpen: true, canDownload: true, canShare: true }
      }
    ]
  };
}

function productInformation(productName: string, volumeLabel: string): CatalogProductInformation {
  return {
    shortDescription: productName,
    fullDescription: `${productName} для ежедневного ухода Best Ori Gin.`,
    usageInstructions: 'Нанесите небольшое количество на чистую кожу.',
    ingredients: 'Вода, глицерин, растительные экстракты, косметическая основа.',
    characteristics: [
      { name: 'Тип кожи', value: 'Для всех типов кожи' },
      { name: 'Объем', value: volumeLabel }
    ]
  };
}
