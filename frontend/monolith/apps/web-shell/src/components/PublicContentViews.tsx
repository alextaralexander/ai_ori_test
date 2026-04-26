import { Button, Card, Col, Input, Row, Space, Tag } from 'antd';
import { useMemo, useState, type ReactElement } from 'react';
import type { ContentCta, ContentPage, ContentSection, DocumentCollection, FaqPage, InfoPage, NewsFeed, OfferPage } from '../api/publicContent';
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

export function FaqPageView({ faq }: { faq: FaqPage }): ReactElement {
  const [query, setQuery] = useState('');
  const filteredItems = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) {
      return faq.items;
    }
    return faq.items.filter((item) => (
      t(item.questionKey).toLowerCase().includes(normalized)
      || t(item.answerKey).toLowerCase().includes(normalized)
      || item.tags.some((tag) => tag.toLowerCase().includes(normalized))
    ));
  }, [faq.items, query]);

  return (
    <div className="page-main" data-testid="faq-page">
      <section className="content-hero">
        <h1>{t('public.faq.title')}</h1>
        <p>{t('public.faq.description')}</p>
        <Input
          allowClear
          data-testid="faq-search"
          onChange={(event) => setQuery(event.target.value)}
          placeholder={t('public.faq.search.placeholder')}
          value={query}
        />
      </section>
      <Row gutter={[16, 16]}>
        <Col xs={24} md={7}>
          <section className="content-list">
            <h2>{t('public.faq.categories.title')}</h2>
            {faq.categories.map((category) => (
              <a href={`/FAQ?category=${category.categoryKey}`} key={category.categoryKey}>
                {t(category.titleKey)} · {category.questionCount}
              </a>
            ))}
          </section>
        </Col>
        <Col xs={24} md={17}>
          {filteredItems.length === 0 ? (
            <UnavailableView testId="faq-empty" messageKey={faq.emptyStateCode} />
          ) : (
            <section className="content-sections">
              {filteredItems.map((item) => (
                <article className="content-section" data-testid={`faq-item-${item.itemKey}`} key={item.itemKey}>
                  <Tag>{t(`public.faq.category.${item.categoryKey}`)}</Tag>
                  <h2>{t(item.questionKey)}</h2>
                  <p>{t(item.answerKey)}</p>
                  <Space wrap>
                    {item.relatedInfoSection ? (
                      <Button data-testid={`faq-related-info-${item.relatedInfoSection}`} href={`/info/${item.relatedInfoSection}`}>
                        {t('public.faq.related.info')}
                      </Button>
                    ) : null}
                    {item.relatedDocumentType ? (
                      <Button href={`/documents/${item.relatedDocumentType}`}>{t('public.faq.related.documents')}</Button>
                    ) : null}
                  </Space>
                </article>
              ))}
            </section>
          )}
        </Col>
      </Row>
    </div>
  );
}

export function InfoPageView({ page }: { page: InfoPage }): ReactElement {
  return (
    <div className="page-main" data-testid="info-page">
      <Breadcrumbs items={page.breadcrumbs} />
      <section className="content-hero">
        <h1>{t(page.titleKey)}</h1>
        {page.descriptionKey ? <p>{t(page.descriptionKey)}</p> : null}
      </section>
      <SectionList sections={page.sections} />
      {page.documents.length > 0 ? (
        <section className="content-list">
          <h2>{t('public.info.relatedDocuments.title')}</h2>
          {page.documents.map((document) => (
            <a data-testid={`info-related-document-${document.documentType}`} href={document.targetRoute} key={document.documentType}>
              {t(document.titleKey)}
            </a>
          ))}
        </section>
      ) : null}
      <CtaList items={page.ctas} />
    </div>
  );
}

export function DocumentsPageView({ collection }: { collection: DocumentCollection }): ReactElement {
  if (collection.documents.length === 0) {
    return <UnavailableView testId="documents-empty" messageKey={collection.emptyStateCode} />;
  }
  return (
    <div className="page-main" data-testid="documents-page">
      <Breadcrumbs items={collection.breadcrumbs} />
      <section className="content-hero">
        <h1>{t(collection.titleKey)}</h1>
        {collection.descriptionKey ? <p>{t(collection.descriptionKey)}</p> : null}
      </section>
      <Row gutter={[16, 16]}>
        {collection.documents.map((document) => (
          <Col xs={24} md={12} key={document.documentKey}>
            <Card data-testid={`document-current-${document.documentKey}`}>
              <Space direction="vertical" size="middle">
                <div>
                  <h2>{t(document.titleKey)}</h2>
                  {document.descriptionKey ? <p>{t(document.descriptionKey)}</p> : null}
                  <small>{t('public.documents.version')} {document.versionLabel} · {new Date(document.publishedAt).toLocaleDateString()}</small>
                </div>
                {document.required ? <Tag data-testid={`document-required-${document.documentKey}`}>{t('public.documents.required')}</Tag> : null}
                <div className="document-viewer" data-testid={`document-viewer-${document.documentKey}`}>{t('public.documents.viewer')}</div>
                <Space wrap>
                  <Button href={document.viewerUrl}>{t('public.documents.view')}</Button>
                  <Button href={document.downloadUrl} type="primary">{t('public.documents.download')}</Button>
                </Space>
                {document.archive.length > 0 ? (
                  <div data-testid={`document-archive-${document.documentKey}`}>
                    <strong>{t('public.documents.archive')}</strong>
                    {document.archive.map((version) => (
                      <a href={version.downloadUrl} key={version.versionLabel}>{version.versionLabel}</a>
                    ))}
                  </div>
                ) : null}
              </Space>
            </Card>
          </Col>
        ))}
      </Row>
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
