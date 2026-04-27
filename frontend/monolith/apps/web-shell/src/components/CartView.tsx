import { Alert, Button, Card, Empty, Space, Tabs } from 'antd';
import type { ReactElement } from 'react';
import { useEffect, useState } from 'react';
import { addCartItem, applyShoppingOffer, changeCartQuantity, loadCart, loadShoppingOffers, loadSupportCart, removeCartLine, type CartLine, type CartResponse, type CartType, type ShoppingOffer } from '../api/cart';
import { t } from '../i18n';

interface CartViewProps {
  cartType?: CartType;
  mode?: 'cart' | 'offers' | 'support';
  seed?: string | null;
  supportUserId?: string;
}

export function CartView({ cartType = 'MAIN', mode = 'cart', seed, supportUserId }: CartViewProps): ReactElement {
  const [cart, setCart] = useState<CartResponse | null>(null);
  const [offers, setOffers] = useState<ShoppingOffer[]>([]);
  const [messageCode, setMessageCode] = useState<string | null>(null);

  useEffect(() => {
    async function load(): Promise<void> {
      if (mode === 'support' && supportUserId) {
        setCart(await loadSupportCart(supportUserId, cartType));
        return;
      }
      if (seed === 'removed-item') {
        setCart(await addCartItem(cartType, { productCode: 'BOG-REMOVED-003', quantity: 1, source: 'PRODUCT_CARD', campaignId: 'CMP-2026-05' }));
        return;
      }
      if (seed === 'checkout-ready') {
        let readyCart = await addCartItem(cartType, { productCode: cartType === 'SUPPLEMENTARY' ? 'BOG-SERUM-002' : 'BOG-CREAM-001', quantity: 1, source: cartType === 'SUPPLEMENTARY' ? 'SUPPLEMENTARY_OFFER' : 'PRODUCT_CARD', campaignId: 'CMP-2026-05' });
        if (cartType === 'MAIN') {
          for (const reason of readyCart.validation.blockingReasons) {
            if (reason.lineId) {
              readyCart = await removeCartLine(reason.lineId);
            }
          }
        }
        setCart(readyCart);
        return;
      }
      setCart(await loadCart(cartType));
      if (mode === 'offers') {
        const loadedOffers = await loadShoppingOffers(cartType);
        setOffers(loadedOffers.offers);
      }
    }
    void load();
  }, [cartType, mode, seed, supportUserId]);

  async function addSupplementaryProduct(): Promise<void> {
    const updated = await addCartItem('SUPPLEMENTARY', { productCode: 'BOG-CREAM-001', quantity: 4, source: 'SUPPLEMENTARY_OFFER', campaignId: 'CMP-2026-05' });
    setCart(updated);
    setMessageCode(updated.messageCode);
  }

  async function increase(line: CartLine): Promise<void> {
    const updated = await changeCartQuantity(line.lineId, line.quantity + 1, cart?.version ?? 0);
    setCart(updated);
    setMessageCode(updated.messageCode);
  }

  async function remove(line: CartLine): Promise<void> {
    const updated = await removeCartLine(line.lineId);
    setCart(updated);
    setMessageCode(updated.messageCode);
  }

  async function applyOffer(offerId: string): Promise<void> {
    const updated = await applyShoppingOffer(cartType, offerId);
    setCart(updated);
    setMessageCode(updated.messageCode);
    const loadedOffers = await loadShoppingOffers(cartType);
    setOffers(loadedOffers.offers);
  }

  const pageTestId = cartType === 'SUPPLEMENTARY'
    ? (mode === 'offers' ? 'supplementary-shopping-offers-page' : 'supplementary-cart-page')
    : (mode === 'offers' ? 'cart-shopping-offers-page' : mode === 'support' ? 'cart-support-view' : 'cart-page');

  return (
    <main className="cart-page" data-testid={pageTestId}>
      <Tabs
        activeKey={mode === 'offers' ? `${cartType}-offers` : cartType}
        items={[
          { key: 'MAIN', label: t('cart.tab.main'), children: null },
          { key: 'MAIN-offers', label: t('cart.tab.offers'), children: null },
          { key: 'SUPPLEMENTARY', label: t('cart.tab.supplementary'), children: null },
          { key: 'SUPPLEMENTARY-offers', label: t('cart.tab.supplementaryOffers'), children: null },
        ]}
      />
      <header className="cart-heading">
        <h1>{t(cartType === 'SUPPLEMENTARY' ? 'cart.supplementary.title' : 'cart.title')}</h1>
        <span data-testid="supplementary-cart-type">{cartType}</span>
      </header>

      {messageCode ? <Alert data-testid="cart-offer-message" message={t(messageCode)} type="success" /> : null}
      {cart?.validation?.blockingReasons?.length ? (
        <Alert data-testid="cart-validation-message" message={t(cart.validation.blockingReasons[0].messageCode)} type="warning" />
      ) : null}

      {mode === 'offers' ? (
        <OfferList cartType={cartType} offers={offers} onApply={(offerId) => void applyOffer(offerId)} />
      ) : (
        <section className="cart-layout">
          <div data-testid={mode === 'support' ? 'cart-support-lines' : undefined}>
            {cart && cart.lines.length > 0 ? cart.lines.map((line) => (
              <CartLineCard key={line.lineId} line={line} onIncrease={() => void increase(line)} onRemove={() => void remove(line)} />
            )) : <Empty description={t('cart.empty')} />}
            {cartType === 'SUPPLEMENTARY' ? (
              <Button data-testid="supplementary-add-BOG-CREAM-001" onClick={() => void addSupplementaryProduct()}>{t('cart.supplementary.add')}</Button>
            ) : null}
          </div>
          <CartTotals cart={cart} />
        </section>
      )}
    </main>
  );
}

function CartLineCard({ line, onIncrease, onRemove }: { line: CartLine; onIncrease: () => void; onRemove: () => void }): ReactElement {
  const prefix = line.source === 'SUPPLEMENTARY_OFFER' ? 'supplementary-line' : 'cart-line';
  return (
    <Card className="cart-line" data-testid={`${prefix}-${line.productCode}`}>
      <div>
        <strong>{line.name}</strong>
        <span>{line.productCode}</span>
      </div>
      <span data-testid={`cart-line-${line.productCode}-availability`}>{line.availability.status}</span>
      <Space>
        <Button onClick={onIncrease} data-testid={`cart-line-${line.productCode}-increase`}>+</Button>
        <span data-testid={`cart-line-${line.productCode}-quantity`}>{line.quantity}</span>
        <Button onClick={onRemove}>{t('cart.remove')}</Button>
      </Space>
      <strong>{line.price.lineTotal}</strong>
    </Card>
  );
}

function CartTotals({ cart }: { cart: CartResponse | null }): ReactElement {
  const checkoutRoute = cart?.cartType === 'SUPPLEMENTARY' ? '/order/supplementary' : '/order';

  return (
    <aside className="cart-totals" data-testid="cart-totals">
      <h2>{t('cart.totals')}</h2>
      <span>{t('cart.subtotal')}: {cart?.totals.subtotal ?? 0}</span>
      <span>{t('cart.discount')}: {cart?.totals.discountTotal ?? 0}</span>
      <span>{t('cart.grandTotal')}: {cart?.totals.grandTotal ?? 0}</span>
      {cart ? (
        <Button
          data-testid={cart.cartType === 'SUPPLEMENTARY' ? 'supplementary-cart-checkout' : 'cart-checkout'}
          disabled={!cart.validation.valid}
          onClick={() => {
            window.location.href = checkoutRoute;
          }}
          type="primary"
        >
          {t('cart.checkout')}
        </Button>
      ) : null}
    </aside>
  );
}

function OfferList({ cartType, offers, onApply }: { cartType: CartType; offers: ShoppingOffer[]; onApply: (offerId: string) => void }): ReactElement {
  return (
    <section className="cart-offers" data-testid={cartType === 'SUPPLEMENTARY' ? 'supplementary-offer-list' : 'cart-offer-list'}>
      {offers.map((offer) => (
        <Card key={offer.offerId} data-testid={`cart-offer-${offer.offerId}`}>
          <h2>{t(offer.titleKey)}</h2>
          <p>{offer.offerType}</p>
          <span data-testid={`cart-offer-${offer.offerId}-status`}>{offer.status}</span>
          <Button data-testid={`cart-offer-${offer.offerId}-apply`} disabled={offer.status === 'UNAVAILABLE'} onClick={() => onApply(offer.offerId)}>
            {t('cart.offer.apply')}
          </Button>
        </Card>
      ))}
    </section>
  );
}
