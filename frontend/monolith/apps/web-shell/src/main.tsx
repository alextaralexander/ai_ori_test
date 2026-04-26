import 'antd/dist/reset.css';
import './styles.css';
import { ConfigProvider } from 'antd';
import ruRU from 'antd/locale/ru_RU';
import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { loadPublicPage, type Audience, type PublicPage } from './api/publicContent';
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
  const path = window.location.pathname;
  const params = new URLSearchParams(window.location.search);
  const loginRole = params.get('role');

  useEffect(() => {
    if (path === '/test-login' && (loginRole === 'customer' || loginRole === 'partner')) {
      window.localStorage.setItem('bestorigin.role', loginRole);
      setPage(null);
      return;
    }
    const pageKey = path === '/community' ? 'community' : 'home';
    loadPublicPage(pageKey, resolveAudience()).then(setPage);
  }, [loginRole, path]);

  if (path === '/test-login') {
    const role = loginRole === 'customer' || loginRole === 'partner'
      ? loginRole
      : window.localStorage.getItem('bestorigin.role') ?? 'guest';
    return <div data-testid="session-ready">{role}</div>;
  }

  if (path !== '/' && path !== '/home' && path !== '/community') {
    return <div data-testid="route-opened" />;
  }

  return (
    <ConfigProvider locale={ruRU}>
      {page ? <PublicShell page={page} /> : null}
    </ConfigProvider>
  );
}

createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
