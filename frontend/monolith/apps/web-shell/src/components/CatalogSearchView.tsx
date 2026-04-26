import { Button, Card, Space } from 'antd';
import type { FormEvent, ReactElement } from 'react';
import { useEffect, useState } from 'react';
import { addCatalogItemToCart, loadCatalogSearch, type CatalogProductCard, type CatalogSearchResponse } from '../api/catalog';
import type { Audience } from '../api/publicContent';
import { t } from '../i18n';

interface CatalogSearchViewProps {
  audience: Audience;
}

export function CatalogSearchView({ audience }: CatalogSearchViewProps): ReactElement {
  const initialParams = new URLSearchParams(window.location.search);
  const [query, setQuery] = useState(initialParams.get('q') ?? '');
  const [category, setCategory] = useState(initialParams.get('category') ?? '');
  const [availability, setAvailability] = useState(initialParams.get('availability') ?? 'all');
  const [promo, setPromo] = useState(initialParams.get('promo') === 'true');
  const [sort, setSort] = useState(initialParams.get('sort') ?? 'relevance');
  const [result, setResult] = useState<CatalogSearchResponse | null>(null);
  const [cartCount, setCartCount] = useState(0);
  const [cartMessage, setCartMessage] = useState<string | null>(null);

  useEffect(() => {
    void load(new URLSearchParams(window.location.search));
  }, [audience]);

  async function load(params: URLSearchParams): Promise<void> {
    setResult(await loadCatalogSearch(params, audience));
  }

  async function submit(event: FormEvent): Promise<void> {
    event.preventDefault();
    const params = buildParams();
    window.history.pushState(null, '', `/search?${params.toString()}`);
    await load(params);
  }

  async function addToCart(product: CatalogProductCard): Promise<void> {
    if (audience === 'GUEST') {
      window.location.href = `/test-login?role=customer&returnUrl=${encodeURIComponent(window.location.pathname + window.location.search)}`;
      return;
    }
    const summary = await addCatalogItemToCart(product.id, audience, window.location.pathname + window.location.search);
    setCartCount(summary.totalQuantity);
    setCartMessage(t(summary.messageCode));
  }

  function buildParams(): URLSearchParams {
    const params = new URLSearchParams();
    if (query) {
      params.set('q', query);
    }
    if (category) {
      params.set('category', category);
    }
    if (availability && availability !== 'all') {
      params.set('availability', availability);
    }
    if (promo) {
      params.set('promo', 'true');
    }
    if (sort && sort !== 'relevance') {
      params.set('sort', sort);
    }
    return params;
  }

  const products = result?.items ?? [];
  const recommendations = result?.recommendations ?? [];

  return (
    <main className="catalog-page" data-testid="catalog-search-page">
      <section className="catalog-toolbar">
        <form onSubmit={(event) => void submit(event)}>
          <label>
            <span>{t('catalog.search.query.label')}</span>
            <input data-testid="catalog-search-input" value={query} onChange={(event) => setQuery(event.target.value)} placeholder={t('catalog.search.query.placeholder')} />
          </label>
          <label>
            <span>{t('catalog.search.category.label')}</span>
            <select data-testid="catalog-category-filter" value={category} onChange={(event) => setCategory(event.target.value)}>
              <option value="">{t('catalog.search.category.all')}</option>
              <option value="face-care">{t('catalog.category.faceCare')}</option>
              <option value="makeup">{t('catalog.category.makeup')}</option>
            </select>
          </label>
          <label>
            <span>{t('catalog.search.availability.label')}</span>
            <select data-testid="catalog-availability-filter" value={availability} onChange={(event) => setAvailability(event.target.value)}>
              <option value="all">{t('catalog.availability.all')}</option>
              <option value="inStock">{t('catalog.availability.inStock')}</option>
              <option value="outOfStock">{t('catalog.availability.outOfStock')}</option>
            </select>
          </label>
          <label>
            <span>{t('catalog.search.sort.label')}</span>
            <select data-testid="catalog-sort-select" value={sort} onChange={(event) => setSort(event.target.value)}>
              <option value="relevance">{t('catalog.sort.relevance')}</option>
              <option value="newest">{t('catalog.sort.newest')}</option>
              <option value="priceAsc">{t('catalog.sort.priceAsc')}</option>
              <option value="priceDesc">{t('catalog.sort.priceDesc')}</option>
              <option value="popular">{t('catalog.sort.popular')}</option>
            </select>
          </label>
          <label className="catalog-checkbox">
            <input data-testid="catalog-promo-filter" type="checkbox" checked={promo} onChange={(event) => setPromo(event.target.checked)} />
            <span>{t('catalog.search.promo.label')}</span>
          </label>
          <Button htmlType="submit" type="primary" data-testid="catalog-search-submit">{t('catalog.search.submit')}</Button>
        </form>
      </section>

      {audience === 'PARTNER' ? <div className="catalog-context" data-testid="catalog-partner-context">{t('catalog.partner.context')}</div> : null}
      <div className="cart-summary" data-testid="cart-summary-count">{cartCount}</div>
      {cartMessage ? <div className="catalog-cart-message" data-testid="catalog-cart-message">{cartMessage}</div> : null}

      {products.length > 0 ? (
        <section className="catalog-grid" aria-label={t('catalog.results.label')}>
          {products.map((product) => <ProductCard key={product.id} audience={audience} product={product} onAdd={() => void addToCart(product)} />)}
        </section>
      ) : result ? (
        <section className="catalog-empty" data-testid="catalog-empty-state">
          <h1>{t(result.messageCode ?? 'STR_MNEMO_CATALOG_SEARCH_EMPTY')}</h1>
          <p>{t('catalog.empty.hint')}</p>
          <Button onClick={() => { setQuery(''); setCategory(''); setAvailability('all'); setPromo(false); setSort('relevance'); }}>{t('catalog.empty.reset')}</Button>
        </section>
      ) : null}

      {products.length === 0 && recommendations.length > 0 ? (
        <section className="catalog-recommendations" data-testid="catalog-recommendations">
          <h2>{t('catalog.recommendations.title')}</h2>
          <div className="catalog-grid">
            {recommendations.map((product) => <ProductCard key={product.id} audience={audience} product={product} onAdd={() => void addToCart(product)} />)}
          </div>
        </section>
      ) : null}
    </main>
  );
}

function ProductCard({ audience, product, onAdd }: { audience: Audience; product: CatalogProductCard; onAdd: () => void }): ReactElement {
  const disabled = audience !== 'GUEST' && !product.canAddToCart;
  return (
    <Card className="catalog-card" data-testid={`catalog-card-${product.sku}`}>
      <div className="catalog-card-image" aria-hidden="true" />
      <h3>{t(product.nameKey)}</h3>
      <p>{t(product.descriptionKey)}</p>
      <Space wrap>
        <span>{t(`catalog.category.${toCategoryKey(product.categorySlug)}`)}</span>
        <span>{product.price} {product.currency}</span>
        <span>{t(`catalog.availability.${product.availability}`)}</span>
      </Space>
      <div className="catalog-badges">
        {product.promoBadges.map((badge) => <span key={badge}>{t(`catalog.badge.${badge}`)}</span>)}
        {product.tags.includes('partner') ? <span>{t('catalog.badge.partner')}</span> : null}
      </div>
      <Button data-testid={`catalog-card-add-${product.sku}`} disabled={disabled} onClick={onAdd}>
        {t('catalog.card.addToCart')}
      </Button>
    </Card>
  );
}

function toCategoryKey(categorySlug: string): string {
  if (categorySlug === 'face-care') {
    return 'faceCare';
  }
  return categorySlug;
}
