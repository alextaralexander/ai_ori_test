import 'antd/dist/reset.css';
import './styles.css';
import { ConfigProvider } from 'antd';
import ruRU from 'antd/locale/ru_RU';
import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { loadContentPage, loadDocuments, loadFaq, loadInfoSection, loadNews, loadOffer, loadPublicPage, type Audience, type BenefitLandingType, type ContentPage, type DocumentCollection, type FaqPage, type InfoPage, type NewsFeed, type OfferPage, type PublicPage } from './api/publicContent';
import { BenefitLandingView } from './components/BenefitLandingView';
import { BonusWalletFinanceView, BonusWalletView } from './components/BonusWalletView';
import { CartView } from './components/CartView';
import { CatalogSearchView } from './components/CatalogSearchView';
import { DigitalCatalogueView } from './components/DigitalCatalogueView';
import { AuthImpersonationView, AuthProvider, AuthRouteForbidden, AuthSessionView } from './components/AuthViews';
import { AdminCatalogView } from './components/AdminCatalogView';
import { AdminCmsView } from './components/AdminCmsView';
import { AdminPimView } from './components/AdminPimView';
import { AdminPricingView } from './components/AdminPricingView';
import { AdminReferralView } from './components/AdminReferralView';
import { AdminRbacView } from './components/AdminRbacView';
import { AdminIdentityView } from './components/AdminIdentityView';
import { AdminOrdersView } from './components/AdminOrdersView';
import { AdminPlatformView } from './components/AdminPlatformView';
import { AdminServiceView } from './components/AdminServiceView';
import { AdminWmsView } from './components/AdminWmsView';
import { EmployeeClaimsView } from './components/EmployeeClaimsView';
import { EmployeeOrderHistoryView } from './components/EmployeeOrderHistoryView';
import { EmployeePartnerCardView } from './components/EmployeePartnerCardView';
import { EmployeeProfileSettingsView } from './components/EmployeeProfileSettingsView';
import { EmployeeWorkspaceView } from './components/EmployeeWorkspaceView';
import { PartnerActivationView, PartnerRegistrationView, SponsorCabinetView } from './components/PartnerOnboardingViews';
import { PartnerGrowthView } from './components/PartnerGrowthView';
import { PartnerOfflineOrdersView } from './components/PartnerOfflineOrdersView';
import { PartnerOfficeView } from './components/PartnerOfficeView';
import { PartnerReportsView } from './components/PartnerReportsView';
import { OrderClaimsView } from './components/OrderClaimsView';
import { OrderCheckoutView } from './components/OrderCheckoutView';
import { OrderHistoryView } from './components/OrderHistoryView';
import { PlatformAnalyticsDiagnosticsView, PlatformConsentState, PlatformConsentView, PlatformExperienceShell } from './components/PlatformExperienceViews';
import { ProductCardView } from './components/ProductCardView';
import { ProfileSettingsView } from './components/ProfileSettingsView';
import { ContentPageView, ContentUnavailableView, DocumentsPageView, FaqPageView, InfoPageView, NewsPage, OfferPageView } from './components/PublicContentViews';
import { PublicShell } from './components/PublicShell';

const testLoginRoles = new Set([
  'customer',
  'partner',
  'partner-leader',
  'business-manager',
  'mlm-analyst',
  'content-manager',
  'catalog-manager',
  'guest',
  'sponsor',
  'invited-partner',
  'employee-support',
  'order-support',
  'support',
  'supervisor',
  'backoffice',
  'finance',
  'accountant',
  'finance-controller',
  'partner-office',
  'partner-office-foreign',
  'logistics-operator',
  'regional-manager',
  'tracking-admin',
  'super-admin',
  'security-admin',
  'master-data-admin',
  'partner-ops-admin',
  'employee-admin',
  'personal-data-auditor',
  'hr-admin',
  'auditor',
  'content-admin',
  'cms-editor',
  'legal-reviewer',
  'marketing-admin',
  'crm-admin',
  'pim-manager',
  'pricing-manager',
  'promotions-manager',
  'logistics-admin',
  'warehouse-operator',
  'order-admin',
  'finance-operator',
  'fraud-admin',
  'audit-admin',
  'support-agent',
  'claim-operator',
  'service-supervisor',
  'wms-integration-operator',
  'category-admin',
  'media-manager',
  'commercial-admin',
  'business-admin',
  'bi-analyst',
  'integration-admin',
  'catalog-manager',
]);

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
  if (path === '/test-login' && loginRole && testLoginRoles.has(loginRole)) {
      window.localStorage.setItem('bestorigin.role', loginRole);
      window.localStorage.setItem('bestorigin.authToken', `test-token-${loginRole}`);
      const invitationCode = params.get('invitationCode') ?? params.get('code');
      if (invitationCode) {
        window.localStorage.setItem('bestorigin.invitationCode', invitationCode);
      }
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
    const role = loginRole && testLoginRoles.has(loginRole)
      ? loginRole
      : window.localStorage.getItem('bestorigin.role') ?? 'guest';
    return <div data-testid="session-ready">{role}</div>;
  }

  if (path === '/catalog' || path === '/partner-office' || path === '/benefits' || path === '/register' || path === '/app' || path === '/sponsor/contact') {
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
  } else if (resolveBenefitRoute(path)) {
    const benefitRoute = resolveBenefitRoute(path);
    contentView = benefitRoute ? (
      <BenefitLandingView
        campaignId={params.get('campaignId')}
        code={benefitRoute.code}
        landingType={benefitRoute.landingType}
        variant={params.get('variant')}
      />
    ) : undefined;
  } else if (path === '/products/digital-catalogue-current') {
    contentView = <DigitalCatalogueView audience={audience} kind="current" preview={params.get('preview') === 'true'} />;
  } else if (path === '/invite/beauty-partner-registration') {
    contentView = (
      <PartnerRegistrationView
        campaignId={params.get('campaignId')}
        code={params.get('code')}
        landingType={params.get('landingType')}
        onboardingType="BEAUTY_PARTNER"
        variant={params.get('variant')}
      />
    );
  } else if (path === '/invite/business-partner-registration') {
    contentView = (
      <PartnerRegistrationView
        campaignId={params.get('campaignId')}
        code={params.get('code')}
        landingType={params.get('landingType')}
        onboardingType="BUSINESS_PARTNER"
        variant={params.get('variant')}
      />
    );
  } else if (path === '/invite/partners-activation') {
    contentView = <PartnerActivationView token={params.get('token') ?? 'ACT-008-001'} />;
  } else if (path === '/invite/sponsor-cabinet') {
    contentView = <SponsorCabinetView />;
  } else if (path === '/products/digital-catalogue-next') {
    contentView = <DigitalCatalogueView audience={audience} kind="next" preview={params.has('preview') ? params.get('preview') === 'true' : undefined} />;
  } else if (path === '/search') {
    contentView = <CatalogSearchView audience={audience} />;
  } else if (path.startsWith('/product/')) {
    contentView = (
      <>
        <PlatformConsentState />
        <ProductCardView audience={audience} productCode={decodeURIComponent(path.slice('/product/'.length))} />
      </>
    );
  } else if (path === '/cart') {
    contentView = <CartView seed={params.get('seed')} />;
  } else if (path === '/cart/shopping-offers') {
    contentView = <CartView mode="offers" />;
  } else if (path === '/cart/supplementary') {
    contentView = <CartView cartType="SUPPLEMENTARY" seed={params.get('seed')} />;
  } else if (path === '/cart/supplementary/shopping-offers') {
    contentView = <CartView cartType="SUPPLEMENTARY" mode="offers" />;
  } else if (path === '/order' || path === '/checkout') {
    contentView = (
      <PlatformExperienceShell>
        <OrderCheckoutView seed={params.get('seed')} />
      </PlatformExperienceShell>
    );
  } else if (path === '/order/supplementary') {
    contentView = <OrderCheckoutView checkoutType="SUPPLEMENTARY" seed={params.get('seed')} />;
  } else if (path === '/order/order-history') {
    contentView = <OrderHistoryView params={params} />;
  } else if (path === '/vip-orders' || path === '/business/tools/order-management/vip-orders/partner-orders') {
    contentView = <PartnerOfflineOrdersView params={params} />;
  } else if (path.startsWith('/business/tools/order-management/vip-orders/partner-orders/')) {
    contentView = <PartnerOfflineOrdersView orderNumber={decodeURIComponent(path.slice('/business/tools/order-management/vip-orders/partner-orders/'.length))} params={params} />;
  } else if (path === '/partner-office/all-orders') {
    contentView = <PartnerOfficeView mode="orders" params={params} />;
  } else if (path === '/partner-office/report') {
    contentView = <PartnerOfficeView mode="report" params={params} />;
  } else if (path === '/auth/session') {
    contentView = <AuthSessionView />;
  } else if (path === '/auth/impersonation') {
    contentView = <AuthImpersonationView />;
  } else if (path === '/employee' && window.localStorage.getItem('bestorigin.role') === 'customer') {
    contentView = <AuthRouteForbidden />;
  } else if (path === '/employee') {
    contentView = <EmployeeWorkspaceView mode="workspace" params={params} />;
  } else if (path === '/employee/new-order') {
    contentView = <EmployeeWorkspaceView mode="new-order" params={params} />;
  } else if (path === '/employee/order-support') {
    contentView = <EmployeeWorkspaceView mode="order-support" params={params} />;
  } else if (path === '/employee/order-history') {
    contentView = <EmployeeOrderHistoryView params={params} />;
  } else if (path.startsWith('/employee/order-history/')) {
    contentView = <EmployeeOrderHistoryView orderId={decodeURIComponent(path.slice('/employee/order-history/'.length))} params={params} />;
  } else if (path === '/employee/submit-claim') {
    contentView = <EmployeeClaimsView mode="create" params={params} />;
  } else if (path === '/employee/claims-history') {
    contentView = <EmployeeClaimsView mode="history" params={params} />;
  } else if (path.startsWith('/employee/claims-history/')) {
    contentView = <EmployeeClaimsView claimId={decodeURIComponent(path.slice('/employee/claims-history/'.length))} mode="details" params={params} />;
  } else if (path === '/employee/partner-card') {
    contentView = <EmployeePartnerCardView mode="card" params={params} />;
  } else if (path === '/employee/report/order-history') {
    contentView = <EmployeePartnerCardView mode="report" params={params} />;
  } else if (path === '/employee/profile-settings') {
    contentView = <EmployeeProfileSettingsView section="overview" />;
  } else if (path === '/employee/profile-settings/general') {
    contentView = <EmployeeProfileSettingsView section="general" />;
  } else if (path === '/employee/profile-settings/contacts') {
    contentView = <EmployeeProfileSettingsView section="contacts" />;
  } else if (path === '/employee/profile-settings/addresses') {
    contentView = <EmployeeProfileSettingsView section="addresses" />;
  } else if (path === '/employee/profile-settings/documents') {
    contentView = <EmployeeProfileSettingsView section="documents" />;
  } else if (path === '/employee/profile-settings/security') {
    contentView = <EmployeeProfileSettingsView section="security" />;
  } else if (path === '/employee/super-user') {
    contentView = <EmployeeProfileSettingsView section="super-user" />;
  } else if (path === '/partner-office/supply') {
    contentView = <PartnerOfficeView mode="supply" params={params} />;
  } else if (path.startsWith('/partner-office/supply/orders/')) {
    contentView = <PartnerOfficeView mode="supply-order" orderNumber={decodeURIComponent(path.slice('/partner-office/supply/orders/'.length))} params={params} />;
  } else if (path.startsWith('/partner-office/supply/')) {
    contentView = <PartnerOfficeView mode="supply-details" params={params} supplyId={decodeURIComponent(path.slice('/partner-office/supply/'.length))} />;
  } else if (path === '/report/order-history') {
    contentView = <PartnerReportsView mode="orders" params={params} />;
  } else if (path === '/report/info-reciept') {
    contentView = <PartnerReportsView mode="documents" params={params} />;
  } else if (path.startsWith('/order/order-history/')) {
    contentView = <OrderHistoryView orderNumber={decodeURIComponent(path.slice('/order/order-history/'.length))} params={params} />;
  } else if (path === '/order/claims/claim-create') {
    contentView = <OrderClaimsView mode="create" params={params} />;
  } else if (path === '/order/claims/claims-history') {
    contentView = <OrderClaimsView mode="history" params={params} />;
  } else if (path.startsWith('/order/claims/claims-history/')) {
    contentView = <OrderClaimsView claimId={decodeURIComponent(path.slice('/order/claims/claims-history/'.length))} mode="details" params={params} />;
  } else if (path === '/profile-settings') {
    contentView = <ProfileSettingsView mode="overview" />;
  } else if (path === '/profile-settings/general') {
    contentView = <ProfileSettingsView mode="general" />;
  } else if (path === '/profile-settings/contacts') {
    contentView = <ProfileSettingsView mode="contacts" />;
  } else if (path === '/profile-settings/addresses') {
    contentView = <ProfileSettingsView mode="addresses" />;
  } else if (path === '/profile-settings/documents') {
    contentView = <ProfileSettingsView mode="documents" />;
  } else if (path === '/profile-settings/security') {
    contentView = <ProfileSettingsView mode="security" />;
  } else if (path.startsWith('/profile-settings/support/')) {
    contentView = <ProfileSettingsView mode="forbidden" />;
  } else if (path.startsWith('/profile/transactions/finance/')) {
    contentView = <BonusWalletFinanceView targetUserId={decodeURIComponent(path.slice('/profile/transactions/finance/'.length))} />;
  } else if (path.startsWith('/profile/transactions/')) {
    contentView = <BonusWalletView params={params} transactionType={decodeURIComponent(path.slice('/profile/transactions/'.length))} />;
  } else if (path === '/business') {
    contentView = (
      <PlatformExperienceShell showLanguageSwitcher>
        <PartnerGrowthView mode="dashboard" params={params} />
      </PlatformExperienceShell>
    );
  } else if (path === '/business/beauty-community') {
    contentView = <PartnerGrowthView mode="community" params={params} />;
  } else if (path === '/business/conversion') {
    contentView = <PartnerGrowthView mode="conversion" params={params} />;
  } else if (path === '/business/team-activity') {
    contentView = <PartnerGrowthView mode="activity" params={params} />;
  } else if (path === '/business/upgrade') {
    contentView = <PartnerGrowthView mode="upgrade" params={params} />;
  } else if (path.startsWith('/business/partner-card/')) {
    contentView = <PartnerGrowthView mode="partner-card" params={params} personNumber={decodeURIComponent(path.slice('/business/partner-card/'.length))} />;
  } else if (path.startsWith('/support/carts/')) {
    contentView = <CartView cartType={(params.get('cartType') === 'SUPPLEMENTARY' ? 'SUPPLEMENTARY' : 'MAIN')} mode="support" supportUserId={decodeURIComponent(path.slice('/support/carts/'.length))} />;
  } else if (path === '/privacy/consent') {
    contentView = <PlatformConsentView />;
  } else if (path === '/admin/analytics-diagnostics') {
    contentView = <PlatformAnalyticsDiagnosticsView />;
  } else if (path === '/admin/rbac') {
    contentView = <AdminRbacView />;
  } else if (path === '/admin/rbac/security') {
    contentView = <AdminRbacView section="security" />;
  } else if (path === '/admin/rbac/service-accounts') {
    contentView = <AdminRbacView section="service-accounts" />;
  } else if (path === '/admin/rbac/audit') {
    contentView = <AdminRbacView section="audit" />;
  } else if (path === '/admin/platform') {
    contentView = <AdminPlatformView />;
  } else if (path === '/admin/platform/integrations') {
    contentView = <AdminPlatformView section="integrations" />;
  } else if (path === '/admin/platform/audit') {
    contentView = <AdminPlatformView section="audit" />;
  } else if (path === '/admin/identity') {
    contentView = <AdminIdentityView />;
  } else if (path.startsWith('/admin/identity/partners/')) {
    contentView = <AdminIdentityView section="partner" />;
  } else if (path === '/admin/identity/impersonation') {
    contentView = <AdminIdentityView section="impersonation" />;
  } else if (path === '/admin/identity/audit') {
    contentView = <AdminIdentityView section="audit" />;
  } else if (path === '/admin/cms') {
    contentView = <AdminCmsView />;
  } else if (path === '/admin/cms/review') {
    contentView = <AdminCmsView section="review" />;
  } else if (path === '/admin/cms/audit') {
    contentView = <AdminCmsView section="audit" />;
  } else if (path === '/admin/referrals') {
    contentView = <AdminReferralView />;
  } else if (path === '/admin/referrals/funnels') {
    contentView = <AdminReferralView section="funnels" />;
  } else if (path === '/admin/referrals/codes') {
    contentView = <AdminReferralView section="codes" />;
  } else if (path === '/admin/referrals/analytics') {
    contentView = <AdminReferralView section="analytics" />;
  } else if (path === '/admin/referrals/audit') {
    contentView = <AdminReferralView section="audit" />;
  } else if (path === '/admin/pim') {
    contentView = <AdminPimView />;
  } else if (path === '/admin/catalogs') {
    contentView = <AdminCatalogView />;
  } else if (path === '/admin/pricing') {
    contentView = <AdminPricingView />;
  } else if (path === '/admin/wms') {
    contentView = <AdminWmsView />;
  } else if (path === '/admin/orders') {
    contentView = <AdminOrdersView />;
  } else if (path === '/admin/service') {
    contentView = <AdminServiceView />;
  } else if (path === '/admin/service/sla-board') {
    contentView = <AdminServiceView section="sla-board" />;
  } else if (path !== '/' && path !== '/home' && path !== '/community') {
    return <div data-testid="route-opened" />;
  }

  return (
    <ConfigProvider locale={ruRU}>
      {page ? <PublicShell contentView={contentView} page={page} /> : null}
    </ConfigProvider>
  );
}

function resolveBenefitRoute(path: string): { landingType: BenefitLandingType; code: string | null } | null {
  if (path === '/beauty-benefits') {
    return { landingType: 'BEAUTY', code: null };
  }
  if (path.startsWith('/beauty-benefits/')) {
    return { landingType: 'BEAUTY', code: decodeURIComponent(path.slice('/beauty-benefits/'.length)) };
  }
  if (path === '/business-benefits') {
    return { landingType: 'BUSINESS', code: null };
  }
  if (path.startsWith('/business-benefits/')) {
    return { landingType: 'BUSINESS', code: decodeURIComponent(path.slice('/business-benefits/'.length)) };
  }
  if (path === '/member-benefits') {
    return { landingType: 'MEMBER', code: null };
  }
  if (path === '/vip-customer-benefits') {
    return { landingType: 'VIP_CUSTOMER', code: null };
  }
  if (path === '/the-new-oriflame-app') {
    return { landingType: 'APP', code: null };
  }
  return null;
}

createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <AuthProvider>
      <App />
    </AuthProvider>
  </React.StrictMode>
);
