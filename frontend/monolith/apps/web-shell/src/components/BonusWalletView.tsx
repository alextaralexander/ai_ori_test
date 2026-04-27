import { Alert, Button, Input, Space } from 'antd';
import type { ReactElement } from 'react';
import { useEffect, useState } from 'react';
import { BonusWalletApiError, exportBonusWalletHistory, getBonusWalletSummary, getBonusWalletTransaction, getFinanceWallet, searchBonusWalletTransactions, type BonusWalletSummaryResponse, type BonusWalletTransactionDetailsResponse, type BonusWalletTransactionPageResponse, type BonusWalletTransactionResponse } from '../api/bonusWallet';
import { t } from '../i18n';

interface BonusWalletViewProps {
  params?: URLSearchParams;
  transactionType: string;
}

export function BonusWalletView({ params = new URLSearchParams(), transactionType }: BonusWalletViewProps): ReactElement {
  const [summary, setSummary] = useState<BonusWalletSummaryResponse | null>(null);
  const [transactions, setTransactions] = useState<BonusWalletTransactionPageResponse | null>(null);
  const [details, setDetails] = useState<BonusWalletTransactionDetailsResponse | null>(null);
  const [orderNumber, setOrderNumber] = useState(params.get('orderNumber') ?? '');
  const [messageCode, setMessageCode] = useState<string | null>(null);
  const [exportMessage, setExportMessage] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    async function load(): Promise<void> {
      setMessageCode(null);
      const loadedSummary = await getBonusWalletSummary(transactionType);
      const loadedTransactions = await searchBonusWalletTransactions(transactionType, params);
      if (active) {
        setSummary(loadedSummary);
        setTransactions(loadedTransactions);
      }
    }
    void load().catch((error: unknown) => {
      setMessageCode(error instanceof BonusWalletApiError ? error.code : 'STR_MNEMO_BONUS_WALLET_EMPTY');
    });
    return () => {
      active = false;
    };
  }, [params, transactionType]);

  async function submitFilters(): Promise<void> {
    const next = new URLSearchParams(params);
    if (orderNumber) {
      next.set('orderNumber', orderNumber);
    } else {
      next.delete('orderNumber');
    }
    const loadedTransactions = await searchBonusWalletTransactions(transactionType, next);
    setTransactions(loadedTransactions);
  }

  async function openDetails(transactionId: string): Promise<void> {
    const loaded = await getBonusWalletTransaction(transactionId);
    setDetails(loaded);
  }

  async function exportHistory(): Promise<void> {
    const result = await exportBonusWalletHistory();
    setExportMessage(result.messageMnemo);
  }

  if (messageCode === 'STR_MNEMO_BONUS_WALLET_ACCESS_DENIED') {
    return <Alert data-testid="bonus-wallet-access-denied" message={`${t(messageCode)} (${messageCode})`} type="error" />;
  }

  return (
    <main className="bonus-wallet-page" data-testid="bonus-wallet-page">
      <header className="bonus-wallet-heading">
        <h1>{t('bonusWallet.title')}</h1>
        <span>{t('bonusWallet.description')}</span>
      </header>
      {messageCode ? <Alert data-testid="bonus-wallet-message" message={`${t(messageCode)} (${messageCode})`} type="warning" /> : null}
      <section className="bonus-wallet-balances">
        {summary?.balances.map((balance) => (
          <article className="bonus-wallet-balance" data-testid={`bonus-wallet-balance-${balance.bucket}`} key={balance.bucket}>
            <h2>{t(`bonusWallet.bucket.${balance.bucket}`)}</h2>
            <strong>{balance.availableAmount} {balance.currencyCode}</strong>
            <span>{t('bonusWallet.hold')}: {balance.holdAmount}</span>
            <span>{t('bonusWallet.expiringSoon')}: {balance.expiringSoonAmount}</span>
          </article>
        ))}
      </section>
      <section className="bonus-wallet-filters">
        <Input data-testid="bonus-wallet-order-filter" onChange={(event) => setOrderNumber(event.target.value)} placeholder={t('bonusWallet.filter.order')} value={orderNumber} />
        <Button data-testid="bonus-wallet-filter-submit" onClick={() => void submitFilters()} type="primary">{t('bonusWallet.filter.submit')}</Button>
        <Button data-testid="bonus-wallet-export" onClick={() => void exportHistory()}>{t('bonusWallet.export')}</Button>
      </section>
      {exportMessage ? <Alert data-testid="bonus-wallet-export-result" message={`${t(exportMessage)} (${exportMessage})`} type="success" /> : null}
      <section className="bonus-wallet-transactions">
        {transactions?.items.map((transaction) => (
          <TransactionRow key={transaction.transactionId} onOpen={() => void openDetails(transaction.transactionId)} transaction={transaction} />
        ))}
      </section>
      {details ? (
        <section className="bonus-wallet-details" data-testid="bonus-wallet-transaction-details">
          <h2>{details.transaction.transactionId}</h2>
          <div>{t(`bonusWallet.bucket.${details.transaction.bucket}`)} · {details.transaction.amount} {details.transaction.currencyCode}</div>
          {details.linkedOrderUrl ? <a data-testid="bonus-wallet-linked-order" href={details.linkedOrderUrl}>{t('bonusWallet.linkedOrder')}</a> : null}
          <div>
            {details.events.map((event) => <div key={`${event.eventType}-${event.occurredAt}`}>{event.messageMnemo ? t(event.messageMnemo) : event.publicStatus}</div>)}
          </div>
        </section>
      ) : null}
    </main>
  );
}

export function BonusWalletFinanceView({ targetUserId }: { targetUserId: string }): ReactElement {
  const [messageCode, setMessageCode] = useState<string | null>(null);

  useEffect(() => {
    void getFinanceWallet(targetUserId).catch((error: unknown) => {
      setMessageCode(error instanceof BonusWalletApiError ? error.code : 'STR_MNEMO_BONUS_WALLET_ACCESS_DENIED');
    });
  }, [targetUserId]);

  return <Alert data-testid="bonus-wallet-access-denied" message={`${t(messageCode ?? 'STR_MNEMO_BONUS_WALLET_ACCESS_DENIED')} (${messageCode ?? 'STR_MNEMO_BONUS_WALLET_ACCESS_DENIED'})`} type="error" />;
}

function TransactionRow({ onOpen, transaction }: { onOpen: () => void; transaction: BonusWalletTransactionResponse }): ReactElement {
  return (
    <article className="bonus-wallet-transaction" data-testid={`bonus-wallet-transaction-${transaction.transactionId}`}>
      <div>
        <strong>{transaction.transactionId}</strong>
        <span>{t(`bonusWallet.bucket.${transaction.bucket}`)} · {transaction.operationType} · {transaction.status}</span>
      </div>
      <span>{transaction.amount} {transaction.currencyCode}</span>
      <span>{transaction.orderNumber ?? transaction.sourceRef}</span>
      <span>{transaction.publicMnemo ? t(transaction.publicMnemo) : transaction.status}</span>
      <Space>
        <Button data-testid={`bonus-wallet-open-${transaction.transactionId}`} onClick={onOpen}>{t('bonusWallet.details')}</Button>
      </Space>
    </article>
  );
}
