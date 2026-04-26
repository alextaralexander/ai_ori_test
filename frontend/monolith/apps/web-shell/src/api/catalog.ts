import type { Audience } from './publicContent';

export interface CatalogProductCard {
  id: string;
  sku: string;
  slug: string;
  nameKey: string;
  descriptionKey: string;
  categorySlug: string;
  imageUrl: string;
  price: number;
  currency: string;
  availability: 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';
  tags: string[];
  promoBadges: string[];
  canAddToCart: boolean;
  unavailableReasonCode?: string | null;
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

export async function addCatalogItemToCart(productId: string, audience: Audience, searchUrl: string): Promise<CartSummaryResponse> {
  const response = await fetch('/api/catalog/cart/items', {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      'Accept-Language': navigator.language
    },
    body: JSON.stringify({
      productId,
      quantity: 1,
      audience,
      userContextId: `${audience.toLowerCase()}-web-session`,
      searchUrl
    })
  });
  if (!response.ok) {
    const error = await response.json().catch(() => ({ messageCode: 'STR_MNEMO_CATALOG_ITEM_UNAVAILABLE' }));
    return { itemsCount: 0, totalQuantity: 0, messageCode: error.messageCode ?? 'STR_MNEMO_CATALOG_ITEM_UNAVAILABLE' };
  }
  return response.json() as Promise<CartSummaryResponse>;
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
      slug: 'hydrating-face-cream',
      nameKey: 'catalog.product.hydratingFaceCream.name',
      descriptionKey: 'catalog.product.hydratingFaceCream.description',
      categorySlug: 'face-care',
      imageUrl: '/assets/catalog/hydrating-face-cream.jpg',
      price: 1290,
      currency: 'RUB',
      availability: 'IN_STOCK',
      tags: ['cream', 'face-care', 'hydration', 'partner'],
      promoBadges: ['new', 'campaign-hit'],
      canAddToCart: true
    },
    {
      id: '22222222-2222-2222-2222-222222222222',
      sku: 'BOG-SERUM-002',
      slug: 'vitamin-glow-serum',
      nameKey: 'catalog.product.vitaminGlowSerum.name',
      descriptionKey: 'catalog.product.vitaminGlowSerum.description',
      categorySlug: 'face-care',
      imageUrl: '/assets/catalog/vitamin-glow-serum.jpg',
      price: 1590,
      currency: 'RUB',
      availability: 'LOW_STOCK',
      tags: ['serum', 'face-care', 'glow'],
      promoBadges: ['campaign-hit'],
      canAddToCart: true
    },
    {
      id: '33333333-3333-3333-3333-333333333333',
      sku: 'BOG-LIP-003',
      slug: 'velvet-lipstick',
      nameKey: 'catalog.product.velvetLipstick.name',
      descriptionKey: 'catalog.product.velvetLipstick.description',
      categorySlug: 'makeup',
      imageUrl: '/assets/catalog/velvet-lipstick.jpg',
      price: 790,
      currency: 'RUB',
      availability: 'OUT_OF_STOCK',
      tags: ['makeup', 'lipstick'],
      promoBadges: ['limited'],
      canAddToCart: false,
      unavailableReasonCode: 'STR_MNEMO_CATALOG_ITEM_UNAVAILABLE'
    }
  ];
}
