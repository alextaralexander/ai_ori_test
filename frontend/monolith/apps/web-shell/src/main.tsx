import 'antd/dist/reset.css';
import './styles.css';
import { ConfigProvider } from 'antd';
import ruRU from 'antd/locale/ru_RU';
import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { loadContentPage, loadDocuments, loadFaq, loadInfoSection, loadNews, loadOffer, loadPublicPage, type Audience, type ContentPage, type DocumentCollection, type FaqPage, type InfoPage, type NewsFeed, type OfferPage, type PublicPage } from './api/publicContent';
import { CatalogSearchView } from './components/CatalogSearchView';
import { DigitalCatalogueView } from './components/DigitalCatalogueView';
import { ProductCardView } from './components/ProductCardView';
import { ContentPageView, ContentUnavailableView, DocumentsPageView, FaqPageView, InfoPageView, NewsPage, OfferPageView } from './components/PublicContentViews';
import { PublicShell } from './components/PublicShell';

function resolveAudience(): Audience {
  const stored = window.localStorage.getItem('bestorigin.role');
  if (stored === 'customer') {
    return 'CUSTOMER';
  }
  if (stored === 'partner') {
    return 'PARTNER';
  }
  if (stored === 'content-manager') {
    return 'CONTENT_MANAGER';
  }
  if (stored === 'catalog-manager') {
    return 'CATALOG_MANAGER';
  }
  return 'GUEST';
}

function App() {
  const [page, setPage] = useState<PublicPage | null>(null);
  const [news, setNews] = useState<NewsFeed | null>(null);
  const [contentPage, setContentPage] = useState<ContentPage | null | undefined>(undefined);
  const [offerPage, setOfferPage] = useState<OfferPage | null | undefined>(undefined);
  const [faqPage, setFaqPage] = useState<FaqPage | null>(null);
  const [infoPage, setInfoPage] = useState<InfoPage | null | undefined>(undefined);
  const [documentsPage, setDocumentsPage] = useState<DocumentCollection | null | undefined>(undefined);
  const path = window.location.pathname;
  const params = new URLSearchParams(window.location.search);
  const loginRole = params.get('role');
  const audience = useMemo(resolveAudience, [path, loginRole]);

  useEffect(() => {
    if (path === '/test-login' && (loginRole === 'customer' || loginRole === 'partner' || loginRole === 'content-manager' || loginRole === 'catalog-manager' || loginRole === 'guest')) {
      window.localStorage.setItem('bestorigin.role', loginRole);
      setPage(null);
      return;
    }
    setNews(null);
    setContentPage(undefined);
    setOfferPage(undefined);
    setFaqPage(null);
    setInfoPage(undefined);
    setDocumentsPage(undefined);
    const shellPageKey = path === '/community' ? 'community' : 'home';
    loadPublicPage(shellPageKey, audience).then(setPage);
    if (path === '/news') {
      loadNews(audience).then(setNews);
    }
    if (path.startsWith('/content/')) {
      loadContentPage(path.slice('/content/'.length), audience).then(setContentPage);
    }
    if (path.startsWith('/offer/')) {
      loadOffer(path.slice('/offer/'.length), audience).then(setOfferPage);
    }
    if (path === '/FAQ' || path === '/faq') {
      loadFaq(audience, params.get('query') ?? '', params.get('category') ?? '').then(setFaqPage);
    }
    if (path === '/info' || path.startsWith('/info/')) {
      loadInfoSection(path === '/info' ? 'overview' : path.slice('/info/'.length), audience).then(setInfoPage);
    }
    if (path.startsWith('/documents/')) {
      loadDocuments(path.slice('/documents/'.length), audience).then(setDocumentsPage);
    }
  }, [audience, loginRole, path]);

  if (path === '/test-login') {
    const role = loginRole === 'customer' || loginRole === 'partner' || loginRole === 'content-manager' || loginRole === 'catalog-manager' || loginRole === 'guest'
      ? loginRole
      : window.localStorage.getItem('bestorigin.role') ?? 'guest';
    return <div data-testid="session-ready">{role}</div>;
  }

  if (path === '/catalog' || path === '/cart' || path === '/partner-office' || path === '/benefits' || path === '/register') {
    return <div data-testid="route-opened" />;
  }

  let contentView: React.ReactElement | undefined;
  if (path === '/news') {
    contentView = news ? <NewsPage feed={news} /> : undefined;
  } else if (path.startsWith('/content/')) {
    contentView = contentPage === null ? <ContentUnavailableView /> : contentPage ? <ContentPageView page={contentPage} /> : undefined;
  } else if (path.startsWith('/offer/')) {
    contentView = offerPage === null ? <ContentUnavailableView /> : offerPage ? <OfferPageView offer={offerPage} /> : undefined;
  } else if (path === '/FAQ' || path === '/faq') {
    contentView = faqPage ? <FaqPageView faq={faqPage} /> : undefined;
  } else if (path === '/info' || path.startsWith('/info/')) {
    contentView = infoPage === null ? <ContentUnavailableView /> : infoPage ? <InfoPageView page={infoPage} /> : undefined;
  } else if (path.startsWith('/documents/')) {
    contentView = documentsPage === null ? <ContentUnavailableView /> : documentsPage ? <DocumentsPageView collection={documentsPage} /> : undefined;
  } else if (path === '/products/digital-catalogue-current') {
    contentView = <DigitalCatalogueView audience={audience} kind="current" preview={params.get('preview') === 'true'} />;
  } else if (path === '/products/digital-catalogue-next') {
    contentView = <DigitalCatalogueView audience={audience} kind="next" preview={params.has('preview') ? params.get('preview') === 'true' : undefined} />;
  } else if (path === '/search') {
    contentView = <CatalogSearchView audience={audience} />;
  } else if (path.startsWith('/product/')) {
    contentView = <ProductCardView audience={audience} productCode={decodeURIComponent(path.slice('/product/'.length))} />;
  } else if (path !== '/' && path !== '/home' && path !== '/community') {
    return <div data-testid="route-opened" />;
  }

  return (
    <ConfigProvider locale={ruRU}>
      {page ? <PublicShell contentView={contentView} page={page} /> : null}
    </ConfigProvider>
  );
}

createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
