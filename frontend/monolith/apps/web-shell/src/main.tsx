import 'antd/dist/reset.css';
import './styles.css';
import { ConfigProvider } from 'antd';
import ruRU from 'antd/locale/ru_RU';
import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { loadContentPage, loadNews, loadOffer, loadPublicPage, type Audience, type ContentPage, type NewsFeed, type OfferPage, type PublicPage } from './api/publicContent';
import { ContentPageView, ContentUnavailableView, NewsPage, OfferPageView } from './components/PublicContentViews';
import { PublicShell } from './components/PublicShell';

function resolveAudience(): Audience {
  const stored = window.localStorage.getItem('bestorigin.role');
  if (stored === 'customer') {
    return 'CUSTOMER';
  }
  if (stored === 'partner') {
    return 'PARTNER';
  }
  return 'GUEST';
}

function App() {
  const [page, setPage] = useState<PublicPage | null>(null);
  const [news, setNews] = useState<NewsFeed | null>(null);
  const [contentPage, setContentPage] = useState<ContentPage | null | undefined>(undefined);
  const [offerPage, setOfferPage] = useState<OfferPage | null | undefined>(undefined);
  const path = window.location.pathname;
  const params = new URLSearchParams(window.location.search);
  const loginRole = params.get('role');
  const audience = useMemo(resolveAudience, [path, loginRole]);

  useEffect(() => {
    if (path === '/test-login' && (loginRole === 'customer' || loginRole === 'partner')) {
      window.localStorage.setItem('bestorigin.role', loginRole);
      setPage(null);
      return;
    }
    setNews(null);
    setContentPage(undefined);
    setOfferPage(undefined);
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
  }, [audience, loginRole, path]);

  if (path === '/test-login') {
    const role = loginRole === 'customer' || loginRole === 'partner'
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
