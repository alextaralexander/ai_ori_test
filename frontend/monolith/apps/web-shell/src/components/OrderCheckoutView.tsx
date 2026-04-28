import { Alert, Button, Input, Space, Steps } from 'antd';
import type { ReactElement } from 'react';
import { useEffect, useMemo, useState } from 'react';
import { applyBenefits, confirmCheckout, OrderApiError, selectDelivery, selectPayment, startCheckout, updateAddress, updateRecipient, validateCheckout, type AddressRequest, type CheckoutDraftResponse, type CheckoutType, type OrderConfirmationResponse } from '../api/order';
import { CheckoutDeliverySection } from './DeliveryViews';
import { t } from '../i18n';

interface OrderCheckoutViewProps {
  checkoutType?: CheckoutType;
  seed?: string | null;
}

const stepKeys = ['order.step.recipient', 'order.step.address', 'order.step.delivery', 'order.step.payment', 'order.step.confirm'];

export function OrderCheckoutView({ checkoutType = 'MAIN', seed }: OrderCheckoutViewProps): ReactElement {
  const [draft, setDraft] = useState<CheckoutDraftResponse | null>(null);
  const [activeStep, setActiveStep] = useState(0);
  const [recipientName, setRecipientName] = useState('');
  const [recipientPhone, setRecipientPhone] = useState('');
  const [address, setAddress] = useState<AddressRequest | null>(null);
  const [deliveryCode, setDeliveryCode] = useState('');
  const [walletAmount, setWalletAmount] = useState('0');
  const [paymentCode, setPaymentCode] = useState('ONLINE_CARD');
  const [validationCode, setValidationCode] = useState<string | null>(null);
  const [confirmation, setConfirmation] = useState<OrderConfirmationResponse | null>(null);

  const cartId = useMemo(() => resolveCartId(checkoutType, seed), [checkoutType, seed]);

  useEffect(() => {
    let active = true;
    async function load(): Promise<void> {
      setValidationCode(null);
      setConfirmation(null);
      const loaded = await startCheckout({
        cartId,
        checkoutType,
        vipMode: checkoutType === 'SUPPLEMENTARY',
        superOrderMode: checkoutType === 'SUPPLEMENTARY',
      });
      if (!active) {
        return;
      }
      setDraft(loaded);
      setRecipientName(loaded.recipient?.fullName ?? '');
      setRecipientPhone(seed === 'invalid-contact' ? '' : loaded.recipient?.phone ?? '');
      setAddress(loaded.address ?? defaultAddress(checkoutType));
      setDeliveryCode(loaded.selectedDelivery?.code ?? loaded.deliveryOptions[0]?.code ?? 'COURIER');
      setPaymentCode(loaded.selectedPayment?.paymentMethodCode ?? 'ONLINE_CARD');
    }
    void load().catch((error: unknown) => {
      if (active) {
        setValidationCode(error instanceof OrderApiError ? error.code : 'STR_MNEMO_ORDER_CHECKOUT_CART_INVALID');
      }
    });
    return () => {
      active = false;
    };
  }, [cartId, checkoutType, seed]);

  async function moveNext(): Promise<void> {
    if (!draft) {
      return;
    }
    setValidationCode(null);
    try {
      if (activeStep === 0) {
        const updated = await updateRecipient(draft.id, {
          recipientType: 'SELF',
          fullName: recipientName,
          phone: recipientPhone,
          email: 'checkout010@example.com',
        });
        setDraft(updated);
      }
      if (activeStep === 1) {
        const updated = await updateAddress(draft.id, address ?? defaultAddress(checkoutType));
        setDraft(updated);
      }
      if (activeStep === 2) {
        const updated = await selectDelivery(draft.id, deliveryCode);
        setDraft(updated);
      }
      if (activeStep === 3) {
        const afterBenefits = await applyBenefits(draft.id, Number(walletAmount) || 0);
        const afterPayment = await selectPayment(afterBenefits.id, paymentCode);
        setDraft(afterPayment);
        const validation = await validateCheckout(afterPayment.id);
        if (!validation.valid) {
          setValidationCode(validation.reasons[0]?.code ?? 'STR_MNEMO_ORDER_CHECKOUT_VALIDATION_FAILED');
          return;
        }
      }
      setActiveStep((step) => Math.min(step + 1, stepKeys.length - 1));
    } catch (error) {
      setValidationCode(error instanceof OrderApiError ? error.code : 'STR_MNEMO_ORDER_CHECKOUT_VALIDATION_FAILED');
    }
  }

  async function confirm(): Promise<void> {
    if (!draft) {
      return;
    }
    setValidationCode(null);
    try {
      const result = await confirmCheckout(draft.id, draft.version);
      setConfirmation(result);
      if (result.reasons.length > 0) {
        setValidationCode(result.reasons[0].code);
      }
    } catch (error) {
      setValidationCode(error instanceof OrderApiError ? error.code : 'STR_MNEMO_ORDER_CHECKOUT_VALIDATION_FAILED');
    }
  }

  return (
    <main className="order-page" data-testid="order-checkout-page">
      <header className="order-heading">
        <div>
          <h1>{t(checkoutType === 'SUPPLEMENTARY' ? 'order.title.supplementary' : 'order.title.main')}</h1>
          <span data-testid="order-checkout-type">{checkoutType}</span>
        </div>
        {checkoutType === 'SUPPLEMENTARY' ? <span data-testid="order-partner-context">{t('order.partner.context')}</span> : null}
      </header>

      {validationCode ? (
        <Alert className="order-validation" data-testid="order-validation-message" message={`${t(validationCode)} (${validationCode})`} type="warning" />
      ) : null}

      <CheckoutDeliverySection />

      <section className="order-layout">
        <div className="order-workspace">
          <Steps
            className="order-stepper"
            current={activeStep}
            data-testid="order-stepper"
            items={stepKeys.map((key) => ({ title: t(key) }))}
            responsive={false}
          />
          <section className="order-step-panel">
            {activeStep === 0 ? (
              <div className="order-form-grid">
                <label>
                  <span>{t('order.recipient.fullName')}</span>
                  <Input data-testid="order-recipient-full-name" onChange={(event) => setRecipientName(event.target.value)} value={recipientName} />
                </label>
                <label>
                  <span>{t('order.recipient.phone')}</span>
                  <Input data-testid="order-recipient-phone" onChange={(event) => setRecipientPhone(event.target.value)} value={recipientPhone} />
                </label>
              </div>
            ) : null}
            {activeStep === 1 ? (
              <Space wrap>
                <Button data-testid="order-address-saved-ADDR-010-MAIN" onClick={() => setAddress(defaultAddress('MAIN'))} type={address?.addressId === 'ADDR-010-MAIN' ? 'primary' : 'default'}>
                  {t('order.address.saved')}
                </Button>
                <Button data-testid="order-pickup-PICKUP-010-01" onClick={() => setAddress(defaultAddress('SUPPLEMENTARY'))} type={address?.pickupPointId === 'PICKUP-010-01' ? 'primary' : 'default'}>
                  {t('order.address.pickup')}
                </Button>
              </Space>
            ) : null}
            {activeStep === 2 ? (
              <div className="order-choice-grid">
                {(draft?.deliveryOptions.length ? draft.deliveryOptions : fallbackDelivery(checkoutType)).map((option) => (
                  <Button data-testid={`order-delivery-${option.code}`} disabled={!option.available} key={option.code} onClick={() => setDeliveryCode(option.code)} type={deliveryCode === option.code ? 'primary' : 'default'}>
                    {t(`order.delivery.${option.code}`)} · {option.price}
                  </Button>
                ))}
              </div>
            ) : null}
            {activeStep === 3 ? (
              <div className="order-form-grid">
                <label>
                  <span>{t('order.wallet.amount')}</span>
                  <Input data-testid="order-wallet-amount" min={0} onChange={(event) => setWalletAmount(event.target.value)} type="number" value={walletAmount} />
                </label>
                <div className="order-choice-grid">
                  <Button data-testid="order-payment-ONLINE_CARD" onClick={() => setPaymentCode('ONLINE_CARD')} type={paymentCode === 'ONLINE_CARD' ? 'primary' : 'default'}>{t('order.payment.ONLINE_CARD')}</Button>
                </div>
              </div>
            ) : null}
            {activeStep === 4 ? <ConfirmationSummary draft={draft} /> : null}
          </section>
          <Space wrap>
            <Button data-testid="order-step-next" disabled={!draft || activeStep >= stepKeys.length - 1} onClick={() => void moveNext()} type="primary">
              {t('order.next')}
            </Button>
            <Button data-testid="order-confirm" disabled={!draft} onClick={() => void confirm()}>
              {t('order.confirm')}
            </Button>
          </Space>
        </div>
        <OrderTotals draft={draft} confirmation={confirmation} />
      </section>
    </main>
  );
}

function ConfirmationSummary({ draft }: { draft: CheckoutDraftResponse | null }): ReactElement {
  return (
    <section className="order-confirmation-summary" data-testid="order-confirmation-summary">
      <h2>{t('order.confirmation.title')}</h2>
      <span>{draft?.items.map((item) => `${item.productCode} x${item.quantity}`).join(', ')}</span>
      <span>{draft?.selectedDelivery?.code ?? 'COURIER'}</span>
      <span>{draft?.selectedPayment?.paymentMethodCode ?? 'ONLINE_CARD'}</span>
    </section>
  );
}

function OrderTotals({ draft, confirmation }: { draft: CheckoutDraftResponse | null; confirmation: OrderConfirmationResponse | null }): ReactElement {
  return (
    <aside className="order-totals" data-testid="order-totals">
      <h2>{t('order.totals')}</h2>
      <span>{t('order.subtotal')}: {draft?.totals.subtotalAmount ?? 0}</span>
      <span>{t('order.delivery')}: {draft?.totals.deliveryAmount ?? 0}</span>
      <span>{t('order.benefits')}: {(draft?.totals.walletAmount ?? 0) + (draft?.totals.cashbackAmount ?? 0) + (draft?.totals.discountAmount ?? 0)}</span>
      <strong>{t('order.grandTotal')}: {draft?.totals.grandTotalAmount ?? 0}</strong>
      {confirmation ? (
        <section className="order-result" data-testid="order-result">
          <h2>{t('order.result.title')}</h2>
          <span data-testid="order-result-number">{confirmation.orderNumber}</span>
          <span data-testid="order-result-type">{confirmation.orderType}</span>
        </section>
      ) : null}
    </aside>
  );
}

function resolveCartId(checkoutType: CheckoutType, seed?: string | null): string {
  if (seed === 'partial-reserve') {
    return 'CART-010-PARTIAL';
  }
  if (seed === 'payment-failed') {
    return 'CART-010-PAYMENT-FAILED';
  }
  return checkoutType === 'SUPPLEMENTARY' ? 'CART-010-SUPP' : 'CART-010-MAIN';
}

function defaultAddress(checkoutType: CheckoutType): AddressRequest {
  if (checkoutType === 'SUPPLEMENTARY') {
    return {
      deliveryTargetType: 'PICKUP_POINT',
      pickupPointId: 'PICKUP-010-01',
    };
  }
  return {
    deliveryTargetType: 'ADDRESS',
    addressId: 'ADDR-010-MAIN',
    country: 'RU',
    city: 'Москва',
    street: 'Тверская',
    house: '10',
    postalCode: '101000',
  };
}

function fallbackDelivery(checkoutType: CheckoutType) {
  return checkoutType === 'SUPPLEMENTARY'
    ? [
      { code: 'PICKUP', name: 'PICKUP', available: true, price: 0, estimatedInterval: '1-2' },
      { code: 'COURIER', name: 'COURIER', available: true, price: 390, estimatedInterval: '2-4' },
    ]
    : [
      { code: 'COURIER', name: 'COURIER', available: true, price: 390, estimatedInterval: '2-4' },
      { code: 'PICKUP', name: 'PICKUP', available: true, price: 0, estimatedInterval: '1-2' },
    ];
}
