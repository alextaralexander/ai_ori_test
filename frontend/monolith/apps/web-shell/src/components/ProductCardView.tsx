import { Button, Card, Space } from 'antd';
import type { ReactElement } from 'react';
import { useEffect, useState } from 'react';
import { loadCatalogProductCard, type CatalogProductCard } from '../api/catalog';
import { addCartItem } from '../api/cart';
import type { Audience } from '../api/publicContent';
import { t } from '../i18n';

interface ProductCardViewProps {
  audience: Audience;
  productCode: string;
}

export function ProductCardView({ audience, productCode }: ProductCardViewProps): ReactElement {
  const [product, setProduct] = useState<CatalogProductCard | null | undefined>(undefined);
  const [quantity, setQuantity] = useState('1');
  const [cartCount, setCartCount] = useState(0);
  const [cartMessage, setCartMessage] = useState<string | null>(null);

  useEffect(() => {
    loadCatalogProductCard(productCode, audience).then(setProduct);
  }, [audience, productCode]);

  async function addToCart(): Promise<void> {
    if (!product) {
      return;
    }
    if (audience === 'GUEST') {
      window.location.href = `/test-login?role=customer&returnUrl=${encodeURIComponent(window.location.pathname)}`;
      return;
    }
    const parsedQuantity = Math.max(Number.parseInt(quantity, 10) || 1, 1);
    const cart = await addCartItem('MAIN', {
      productCode: product.productCode ?? product.sku,
      quantity: parsedQuantity,
      source: 'PRODUCT_CARD',
      campaignId: product.campaignCode,
    });
    const currentLine = cart.lines.find((line) => line.productCode === (product.productCode ?? product.sku) && line.source === 'PRODUCT_CARD');
    setCartCount(currentLine?.quantity ?? parsedQuantity);
    setCartMessage(t(cart.messageCode === 'STR_MNEMO_CART_RECALCULATED' ? 'STR_MNEMO_CART_ITEM_ADDED' : cart.messageCode));
  }

  if (product === undefined) {
    return <main className="product-card-page" data-testid="product-card-loading">{t('catalog.product.loading')}</main>;
  }

  if (product === null) {
    return (
      <main className="product-card-page product-card-empty" data-testid="product-card-not-found">
        <h1>{t('STR_MNEMO_CATALOG_PRODUCT_NOT_FOUND')}</h1>
        <Button href="/search" data-testid="product-card-back-to-search">{t('catalog.product.backToSearch')}</Button>
      </main>
    );
  }

  const media = product.media?.length ? product.media : [{ url: product.imageUrl, altText: product.name ?? t(product.nameKey), primary: true, sortOrder: 1 }];
  const recommendations = product.recommendations ?? [];

  return (
    <main className="product-card-page" data-testid="product-card-page">
      <div className="cart-summary" data-testid="cart-summary-count">{cartCount}</div>
      {cartMessage ? <div className="catalog-cart-message" data-testid="product-card-cart-message">{cartMessage}</div> : null}
      {audience === 'PARTNER' ? <div className="catalog-context" data-testid="product-card-partner-context">{t('catalog.partner.context')}</div> : null}

      <section className="product-card-layout">
        <div className="product-gallery" data-testid="product-card-gallery" aria-label={t('catalog.product.gallery')}>
          {media.map((item) => <div key={`${item.url}-${item.sortOrder}`} className="product-gallery-item">{item.altText}</div>)}
        </div>

        <Card className="product-buybox">
          <p className="product-code">{product.productCode ?? product.sku}</p>
          <h1 data-testid="product-card-title">{product.name ?? t(product.nameKey)}</h1>
          <Space wrap>
            <span>{product.brand}</span>
            <span>{product.volumeLabel}</span>
            <span>{product.categoryName ?? t(`catalog.category.${toCategoryKey(product.categorySlug)}`)}</span>
          </Space>
          <div className="product-price">
            <strong>{product.price} {product.currency}</strong>
          </div>
          <div data-testid="product-card-availability">{t(`catalog.availability.${product.availability}`)}</div>
          <label className="product-quantity">
            <span>{t('catalog.product.quantity')}</span>
            <input
              data-testid="product-card-quantity"
              min={product.orderLimits?.minQuantity ?? 1}
              max={product.orderLimits?.maxQuantity ?? 99}
              type="number"
              value={quantity}
              onChange={(event) => setQuantity(event.target.value)}
            />
          </label>
          <Button data-testid="product-card-add-to-cart" onClick={() => void addToCart()} type="primary">
            {t('catalog.card.addToCart')}
          </Button>
          <Button data-testid="product-card-checkout" disabled={cartCount === 0} onClick={() => { window.location.href = '/checkout'; }}>
            {t('catalog.product.checkout')}
          </Button>
        </Card>
      </section>

      <section className="product-info" data-testid="product-card-information">
        <h2>{t('catalog.product.details')}</h2>
        <p>{product.information?.fullDescription ?? t(product.descriptionKey)}</p>
        <h3>{t('catalog.product.usage')}</h3>
        <p>{product.information?.usageInstructions}</p>
        <h3>{t('catalog.product.ingredients')}</h3>
        <p>{product.information?.ingredients}</p>
        <div className="product-characteristics">
          {product.information?.characteristics?.map((item) => (
            <div key={item.name}>
              <strong>{item.name}</strong>
              <span>{item.value}</span>
            </div>
          ))}
        </div>
      </section>

      {recommendations.length > 0 ? (
        <section className="product-recommendations" data-testid="product-card-recommendations">
          <h2>{t('catalog.recommendations.title')}</h2>
          <div className="catalog-grid">
            {recommendations.map((item) => (
              <a key={item.productCode} className="product-recommendation-card" href={`/product/${item.productCode}`}>
                <span>{item.name}</span>
                <span>{item.price} {item.currency}</span>
              </a>
            ))}
          </div>
        </section>
      ) : <section className="product-recommendations" data-testid="product-card-recommendations" />}
    </main>
  );
}

function toCategoryKey(categorySlug: string): string {
  if (categorySlug === 'face-care') {
    return 'faceCare';
  }
  return categorySlug;
}
