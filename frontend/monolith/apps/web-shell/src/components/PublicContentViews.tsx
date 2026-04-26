import { Button, Card, Col, Row, Space } from 'antd';
import type { ReactElement } from 'react';
import type { ContentCta, ContentPage, ContentSection, NewsFeed, OfferPage } from '../api/publicContent';
import { t } from '../i18n';

export function NewsPage({ feed }: { feed: NewsFeed }): ReactElement {
  if (feed.items.length === 0) {
    return <UnavailableView testId="news-empty" messageKey={feed.emptyStateCode} />;
  }

  return (
    <div className="page-main">
      <section className="content-hero" data-testid="news-feed">
        <h1>{t('public.news.title')}</h1>
        <p>{t('public.news.description')}</p>
      </section>
      <Row gutter={[16, 16]}>
        {feed.items.map((item) => (
          <Col xs={24} md={12} lg={8} key={item.newsKey}>
            <a className="news-card" href={item.targetRoute} data-testid={`news-card-${item.newsKey}`}>
              <Card>
                <span>{item.categoryKey ? t(item.categoryKey) : t('public.news.category.default')}</span>
                <h2>{t(item.titleKey)}</h2>
                {item.summaryKey ? <p>{t(item.summaryKey)}</p> : null}
                <small>{new Date(item.publishedAt).toLocaleDateString()}</small>
              </Card>
            </a>
          </Col>
        ))}
      </Row>
    </div>
  );
}

export function ContentPageView({ page }: { page: ContentPage }): ReactElement {
  return (
    <div className="page-main" data-testid="content-page">
      <Breadcrumbs items={page.breadcrumbs} />
      <section className="content-hero">
        <h1>{t(page.titleKey)}</h1>
        {page.descriptionKey ? <p>{t(page.descriptionKey)}</p> : null}
      </section>
      <SectionList sections={page.sections} />
      <Attachments items={page.attachments} />
      <ProductLinks items={page.productLinks} />
      <CtaList items={page.ctas} />
    </div>
  );
}

export function OfferPageView({ offer }: { offer: OfferPage }): ReactElement {
  return (
    <div className="page-main" data-testid="offer-page">
      <Breadcrumbs items={offer.breadcrumbs} />
      <section className="content-hero offer-hero">
        <h1>{t(offer.hero.titleKey)}</h1>
        {offer.hero.summaryKey ? <p>{t(offer.hero.summaryKey)}</p> : null}
        <CtaList items={offer.ctas} primaryTestId="offer-primary-cta" />
      </section>
      <SectionList sections={offer.sections} />
      <Attachments items={offer.attachments} />
      <ProductLinks items={offer.productLinks} />
    </div>
  );
}

export function ContentUnavailableView(): ReactElement {
  return <UnavailableView testId="content-unavailable" messageKey="STR_MNEMO_PUBLIC_CONTENT_NOT_FOUND" />;
}

function Breadcrumbs({ items }: { items: { labelKey: string; route: string }[] }): ReactElement {
  return (
    <nav className="breadcrumbs" aria-label={t('public.breadcrumbs.label')}>
      {items.map((item) => (
        <a key={item.route} href={item.route}>{t(item.labelKey)}</a>
      ))}
    </nav>
  );
}

function SectionList({ sections }: { sections: ContentSection[] }): ReactElement {
  return (
    <section className="content-sections">
      {sections.map((section) => (
        <article
          className="content-section"
          data-testid={section.sectionType === 'RICH_TEXT' ? 'content-section-rich-text' : section.sectionType === 'CONDITIONS' ? 'offer-conditions' : `content-section-${section.sectionKey}`}
          key={section.sectionKey}
        >
          {section.payload.titleKey ? <h2>{t(section.payload.titleKey)}</h2> : null}
          {section.payload.bodyKey ? <p>{t(section.payload.bodyKey)}</p> : null}
        </article>
      ))}
    </section>
  );
}

function Attachments({ items }: { items: { attachmentKey: string; titleKey: string; url: string; fileType: string }[] }): ReactElement | null {
  if (items.length === 0) {
    return null;
  }
  return (
    <section className="content-list">
      <h2>{t('public.content.attachments.title')}</h2>
      {items.map((item) => (
        <a data-testid="content-attachment-pdf" key={item.attachmentKey} href={item.url}>
          {t(item.titleKey)}
        </a>
      ))}
    </section>
  );
}

function ProductLinks({ items }: { items: { productRef: string; labelKey?: string; targetRoute: string }[] }): ReactElement | null {
  if (items.length === 0) {
    return null;
  }
  return (
    <section className="content-list">
      <h2>{t('public.content.products.title')}</h2>
      {items.map((item) => (
        <a key={item.productRef} href={item.targetRoute}>
          {t(item.labelKey ?? 'public.navigation.catalog')}
        </a>
      ))}
    </section>
  );
}

function CtaList({ items, primaryTestId }: { items: ContentCta[]; primaryTestId?: string }): ReactElement {
  return (
    <Space wrap>
      {items.map((item, index) => (
        <Button
          data-testid={item.audience === 'CUSTOMER' ? 'content-cta-customer-cart' : index === 0 ? primaryTestId : undefined}
          href={item.targetValue}
          key={`${item.labelKey}-${item.targetValue}`}
          type={index === 0 ? 'primary' : 'default'}
        >
          {t(item.labelKey)}
        </Button>
      ))}
    </Space>
  );
}

function UnavailableView({ testId, messageKey }: { testId: string; messageKey: string }): ReactElement {
  return (
    <div className="page-main" data-testid={testId}>
      <section className="content-hero">
        <h1>{t(messageKey)}</h1>
        <Space wrap>
          <Button href="/news">{t('public.navigation.news')}</Button>
          <Button type="primary" href="/catalog">{t('public.navigation.catalog')}</Button>
        </Space>
      </section>
    </div>
  );
}
