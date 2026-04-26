import { Button, Card, Col, Layout, Row, Space } from 'antd';
import type { ReactElement } from 'react';
import type { EntryPoint, PublicPage } from '../api/publicContent';
import { t } from '../i18n';

const { Content, Footer, Header } = Layout;

interface PublicShellProps {
  page: PublicPage;
}

export function PublicShell({ page }: PublicShellProps): ReactElement {
  const headerItems = page.navigation.filter((item) => item.area === 'HEADER');
  const footerItems = page.navigation.filter((item) => item.area === 'FOOTER');
  const hero = page.blocks.find((block) => block.blockType === 'HERO');
  const promos = page.blocks.filter((block) => block.blockType === 'PROMO');
  const isCommunity = page.pageKey === 'COMMUNITY';

  return (
    <Layout className="public-shell">
      <Header className="public-header">
        <a className="brand" href="/">{t('brand.name')}</a>
        <nav className="header-nav" aria-label={t('brand.name')}>
          {headerItems.map((item) => (
            <a key={item.itemKey} href={item.targetValue}>{t(item.labelKey)}</a>
          ))}
          <a href="/login">{t('public.navigation.login')}</a>
        </nav>
      </Header>
      <Content>
        {isCommunity ? <CommunityPage page={page} /> : <HomePage page={page} hero={hero} promos={promos} />}
      </Content>
      <Footer className="public-footer">
        <nav aria-label={t('public.navigation.documents')}>
          {footerItems.map((item) => (
            <a key={item.itemKey} href={item.targetValue}>{t(item.labelKey)}</a>
          ))}
        </nav>
      </Footer>
    </Layout>
  );
}

function HomePage({ page, hero, promos }: { page: PublicPage; hero?: PublicPage['blocks'][number]; promos: PublicPage['blocks'] }): ReactElement {
  return (
    <div className="page-main">
      <section className="hero" data-testid="home-hero">
        <div>
          <h1>{t(hero?.payload.titleKey ?? 'public.home.hero.title')}</h1>
          <p>{t(hero?.payload.descriptionKey ?? 'public.home.hero.description')}</p>
          <Space wrap>
            <Button type="primary" href="/catalog">{t('public.home.hero.primaryCta')}</Button>
            <Button href="/register">{t('public.home.hero.secondaryCta')}</Button>
          </Space>
        </div>
        <div className="hero-media" aria-hidden="true" />
      </section>
      <EntryPoints entries={page.entryPoints} />
      <section className="promo-section" data-testid="home-promo-section">
        <h2>{t('public.home.promo.current.title')}</h2>
        <Row gutter={[16, 16]}>
          {promos.map((promo) => (
            <Col xs={24} md={8} key={promo.blockKey}>
              <Card>
                <h3>{t(promo.payload.titleKey)}</h3>
                <Button href="/catalog">{t('public.navigation.catalog')}</Button>
              </Card>
            </Col>
          ))}
        </Row>
      </section>
    </div>
  );
}

function CommunityPage({ page }: { page: PublicPage }): ReactElement {
  return (
    <div className="page-main">
      <section className="community-overview" data-testid="community-overview">
        <h1>{t('public.community.overview.title')}</h1>
        <p>{t('public.community.overview.description')}</p>
        <Button type="primary" href="/register">{t('public.navigation.register')}</Button>
      </section>
      <EntryPoints entries={page.entryPoints} />
    </div>
  );
}

function EntryPoints({ entries }: { entries: EntryPoint[] }): ReactElement {
  return (
    <section className="entry-section">
      <h2>{t('public.home.quickLinks.title')}</h2>
      <Row gutter={[16, 16]}>
        {entries.map((entry) => (
          <Col xs={24} sm={12} lg={6} key={entry.entryKey}>
            <a className="entry-card" href={entry.targetRoute} data-testid={`entry-${toTestId(entry.entryKey)}`}>
              <strong>{t(entry.labelKey)}</strong>
              {entry.descriptionKey ? <span>{t(entry.descriptionKey)}</span> : null}
            </a>
          </Col>
        ))}
      </Row>
    </section>
  );
}

function toTestId(entryKey: string): string {
  return entryKey.replace(/[A-Z]/g, (char) => `-${char.toLowerCase()}`);
}
