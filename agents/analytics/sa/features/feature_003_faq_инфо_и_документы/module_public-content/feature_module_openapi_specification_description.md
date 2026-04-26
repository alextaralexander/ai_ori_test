# Feature 003. Описание OpenAPI public-content

## Новые endpoints
- `GET /api/public-content/faq`: возвращает категории FAQ, вопросы, пустое состояние и поддерживает параметры `audience`, `category`, `query`.
- `GET /api/public-content/info/{section}`: возвращает информационный раздел с breadcrumbs, SEO, секциями, связанными документами и CTA.
- `GET /api/public-content/documents/{documentType}`: возвращает документы выбранного типа, актуальную редакцию, архив версий, PDF viewer URL и download URL.

## Контракт сообщений
Backend не возвращает предопределенные пользовательские тексты напрямую. Для ошибок и пустых состояний используются mnemonic-коды `STR_MNEMO_PUBLIC_FAQ_EMPTY`, `STR_MNEMO_PUBLIC_INFO_NOT_FOUND`, `STR_MNEMO_PUBLIC_DOCUMENTS_NOT_FOUND` и `STR_MNEMO_PUBLIC_CONTENT_UNAVAILABLE`.

## Ролевой доступ
Параметр `audience` фильтрует материалы по аудитории. `GUEST` видит только публичные материалы, `CUSTOMER` видит публичные и клиентские, `PARTNER` видит публичные и партнерские.
