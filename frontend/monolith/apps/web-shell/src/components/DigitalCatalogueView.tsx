import { Button, Space } from 'antd';
import type { ReactElement } from 'react';
import { useEffect, useMemo, useState } from 'react';
import { loadDigitalCatalogue, runDigitalCatalogueMaterialAction, type DigitalCatalogueIssue, type DigitalCatalogueMaterial } from '../api/catalog';
import type { Audience } from '../api/publicContent';
import { t } from '../i18n';

interface DigitalCatalogueViewProps {
  audience: Audience;
  kind: 'current' | 'next';
  preview?: boolean;
}

export function DigitalCatalogueView({ audience, kind, preview }: DigitalCatalogueViewProps): ReactElement {
  const [catalogue, setCatalogue] = useState<DigitalCatalogueIssue | null | undefined>(undefined);
  const [messageCode, setMessageCode] = useState<string | null>(null);
  const [pageIndex, setPageIndex] = useState(0);
  const [selectedMaterial, setSelectedMaterial] = useState<DigitalCatalogueMaterial | null>(null);
  const [zoom, setZoom] = useState(100);
  const [actionMessage, setActionMessage] = useState<string | null>(null);
  const [shareUrl, setShareUrl] = useState<string | null>(null);

  useEffect(() => {
    setCatalogue(undefined);
    setMessageCode(null);
    loadDigitalCatalogue(kind, audience, preview).then((result) => {
      if ('messageCode' in result) {
        setCatalogue(null);
        setMessageCode(result.messageCode);
        return;
      }
      setCatalogue(result);
      setSelectedMaterial(result.materials[0] ?? null);
    });
  }, [audience, kind, preview]);

  const currentPage = useMemo(() => catalogue?.pages[pageIndex], [catalogue, pageIndex]);

  async function runAction(action: 'download' | 'share'): Promise<void> {
    if (!selectedMaterial) {
      return;
    }
    const result = await runDigitalCatalogueMaterialAction(selectedMaterial.materialId, action, audience);
    setActionMessage(t(result.messageCode));
    if (action === 'share') {
      setShareUrl(result.url || window.location.href);
    }
  }

  if (catalogue === undefined) {
    return <main className="digital-catalogue-page" data-testid="digital-catalogue-loading">{t('catalog.digital.loading')}</main>;
  }

  if (catalogue === null) {
    return (
      <main className="digital-catalogue-page digital-catalogue-state" data-testid="digital-catalogue-forbidden">
        <h1>{t(messageCode ?? 'STR_MNEMO_DIGITAL_CATALOGUE_NOT_FOUND')}</h1>
        <Button href="/products/digital-catalogue-current">{t('catalog.digital.openCurrent')}</Button>
      </main>
    );
  }

  return (
    <main className="digital-catalogue-page" data-testid="digital-catalogue-page">
      <header className="digital-catalogue-header">
        <span>{t(`catalog.digital.periodType.${catalogue.periodType}`)}</span>
        <h1>{catalogue.title}</h1>
        <div data-testid="digital-catalogue-period">
          {formatDate(catalogue.period.startDate)} - {formatDate(catalogue.period.endDate)}
        </div>
        <div>{t(`catalog.digital.status.${catalogue.publicationStatus}`)}</div>
      </header>

      <section className="digital-catalogue-layout">
        <aside className="digital-catalogue-materials" data-testid="digital-catalogue-materials">
          <h2>{t('catalog.digital.materials')}</h2>
          {catalogue.materials.map((material) => (
            <Button
              block
              data-testid={`digital-catalogue-material-${material.materialId}`}
              key={material.materialId}
              onClick={() => setSelectedMaterial(material)}
            >
              {material.title}
            </Button>
          ))}
        </aside>

        <section className="digital-catalogue-viewer" data-testid="digital-catalogue-viewer" aria-label={t('catalog.digital.viewer')}>
          <div className="digital-catalogue-viewer-toolbar">
            <Space wrap>
              <Button data-testid="digital-catalogue-zoom-in" onClick={() => setZoom((value) => Math.min(value + 10, 160))}>{t('catalog.digital.zoomIn')}</Button>
              <Button data-testid="digital-catalogue-zoom-out" onClick={() => setZoom((value) => Math.max(value - 10, 70))}>{t('catalog.digital.zoomOut')}</Button>
              <Button data-testid="digital-catalogue-download" disabled={!selectedMaterial?.actions.canDownload} onClick={() => void runAction('download')}>{t('catalog.digital.download')}</Button>
              <Button data-testid="digital-catalogue-share" disabled={!selectedMaterial?.actions.canShare} onClick={() => void runAction('share')}>{t('catalog.digital.share')}</Button>
            </Space>
          </div>

          <div className="digital-catalogue-page-frame" style={{ transform: `scale(${zoom / 100})` }}>
            <div className="digital-catalogue-page-preview">{selectedMaterial?.title ?? catalogue.title}</div>
            {currentPage?.hotspots.map((hotspot) => (
              <button
                aria-label={t('catalog.digital.openProduct')}
                className="digital-catalogue-hotspot"
                data-testid={`digital-catalogue-hotspot-${hotspot.productCode}`}
                key={hotspot.productCode}
                onClick={() => { window.location.href = `/product/${hotspot.productCode}?campaignCode=${catalogue.issueCode}`; }}
                style={{ left: `${hotspot.xPercent}%`, top: `${hotspot.yPercent}%`, width: `${hotspot.widthPercent}%`, height: `${hotspot.heightPercent}%` }}
                type="button"
              />
            ))}
          </div>

          <div className="digital-catalogue-pagination">
            <Button disabled={pageIndex === 0} onClick={() => setPageIndex((value) => Math.max(value - 1, 0))}>{t('catalog.digital.previousPage')}</Button>
            <span data-testid="digital-catalogue-page-number">{pageIndex + 1} / {catalogue.pages.length}</span>
            <Button data-testid="digital-catalogue-next-page" disabled={pageIndex >= catalogue.pages.length - 1} onClick={() => setPageIndex((value) => Math.min(value + 1, catalogue.pages.length - 1))}>{t('catalog.digital.nextPage')}</Button>
          </div>

          {actionMessage ? <div data-testid="digital-catalogue-action-message">{actionMessage}</div> : null}
          {shareUrl ? <div data-testid="digital-catalogue-share-url">{shareUrl}</div> : null}
        </section>
      </section>
    </main>
  );
}

function formatDate(value: string): string {
  return new Intl.DateTimeFormat(undefined).format(new Date(value));
}
