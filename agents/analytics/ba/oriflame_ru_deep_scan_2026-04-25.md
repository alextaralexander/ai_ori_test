# Повторный deep-scan `oriflame.ru` от 2026-04-25

## Цель ревизии
Повторно проверить, что feature backlog для Best Ori Gin покрывает не только видимые SPA-маршруты donor-сайта, но и скрытые бизнес-контуры, подтвержденные support-страницами, маркетинговыми лендингами и PWA-слоем.

## Источники проверки
- Живой SPA manifest: `https://oriflame.ru/assets/manifest-c6aae4ba.js`
- Главная и PWA-слой: `https://oriflame.ru/`, `https://oriflame.ru/manifest.webmanifest`
- Маркетинговые и продуктовые страницы:
  - `https://www.oriflame.ru/the-new-oriflame-app`
  - `https://www.oriflame.ru/member-benefits`
  - `https://www.oriflame.ru/vip-customer-benefits`
  - `https://www.oriflame.ru/business-opportunity/about-products`
- Support center и правила:
  - `https://www.oriflame.ru/support-center/registration-membership-profile/membership/benefits-member-reward-plan`
  - `https://www.oriflame.ru/support-center/registration-membership-profile/membership/what-is-cashback`
  - `https://www.oriflame.ru/support-center/registration-membership-profile/membership/what-is-sharing-discount`
  - `https://www.oriflame.ru/support-center/registration-membership-profile/oriflame-wallet/what-is-oriflame-wallet`
  - `https://www.oriflame.ru/support-center/registration-membership-profile/membership/what-is-welcome-programme`
  - `https://www.oriflame.ru/support-center/ordering-claims-returns-delivery/OrderingBrP/ordering-on-weekends`
  - `https://www.oriflame.ru/support-center/ordering-claims-returns-delivery/orderingbrp/reservation`
  - `https://www.oriflame.ru/support-center/product-business-tools-and-programs/about-wellness/wellness-subscription`
  - `https://www.oriflame.ru/about/oriflame-rules-of-conduct`

## Покрытие по глубине
- Требуемая глубина обхода принята равной 10 уровням.
- По живому manifest максимальная фактическая глубина route path составила 6 сегментов.
- Это означает, что весь доступный SPA route graph был покрыт полностью, а запас до 10 уровней использован для проверки вложенных веток `/business/tools/...`, `/partner-office/supply/...`, `/employee/profile-settings/...`, `/order/claims/...`.

## Что подтверждено по живому route graph
- Публичная витрина, новости, контент, документы, FAQ, поиск, карточка товара и цифровые каталоги действительно существуют как отдельные route-группы.
- Существуют отдельные приватные ветки для:
  - личного заказа и supplementary order;
  - истории заказов и претензий;
  - MLM-кабинета, team activity, conversion, upgrade;
  - партнерских офлайн-заказов клиентов;
  - partner office supply flows;
  - employee-ветки с super-user режимом.
- В root bundle и базовом layout явно присутствуют `mindbox`, `metrika`, `offline`, `notification`, `push`, `wallet`, `payment`, что подтверждает отдельный системный слой коммуникаций, аналитики, кошелька и офлайн-обработки.

## Что найдено дополнительно вне route graph

### 1. Отдельная программа преимуществ бьюти-партнера
Support и маркетинговые страницы подтверждают, что у Oriflame есть не только MLM reward plan для бизнес-партнера, но и отдельный пользовательский benefit-контур:
- приветственная скидка;
- кешбэк 15% на личные заказы;
- рекомендационная скидка 10% за первый уровень;
- бесплатная доставка по условиям каталога;
- стартовые программы и reward-предложения.

Вывод для Best Ori Gin:
нельзя сводить этот контур только к `кошельку` или только к `MLM бонусам`; это отдельная предметная область.

### 2. Электронный кошелек - это не просто ledger
Support-страницы подтверждают, что электронный кошелек:
- хранит как кешбэк, так и рекомендационную скидку;
- используется для оплаты следующих заказов;
- имеет лимит применения к заказу;
- живет в рамках текущего и нескольких следующих каталогов;
- получает отрицательные корректировки при возвратах.

Вывод для Best Ori Gin:
кошелек нужно моделировать как полноценный кошелек скидок, связанный с заказами, а не как абстрактный бонусный баланс.

### 3. Social selling и личная реферальная ссылка - самостоятельный контур
Маркетинговые и юридические страницы подтверждают:
- после активации у партнера появляется личная реферальная ссылка;
- ссылка используется как единственный одобренный online-selling инструмент;
- рекомендации друзьям и первый уровень заказов влияют на скидки и выгоды;
- есть отдельная логика атрибуции sponsor/referral source.

Вывод для Best Ori Gin:
реферальную механику нельзя ограничивать только первичной регистрацией партнера; нужен отдельный post-activation контур social selling.

### 4. 3-недельный каталог - это не только PDF
Контентные и support-страницы подтверждают, что каждые 3 недели меняются:
- каталог;
- маркетинговые и pricing-правила;
- сроки действия кешбэка, рекомендационных скидок, резерва и welcome/retention механик;
- операционные окна, включая техническое обновление в конце 3-й недели каталога.

Вывод для Best Ori Gin:
catalog campaign lifecycle должен быть описан как сквозной операционный механизм, а не только как публикация PDF-каталога.

### 5. Резервирование и подписки - это отдельные retention/order mechanics
Support-страницы подтверждают:
- резерв товара действует несколько каталогов;
- reserved product потом автоматически возвращается в order flow;
- подписки имеют собственный пошаговый цикл, скидочную механику и правила восстановления.

Вывод для Best Ori Gin:
в backlog нужно явно учитывать reservation/subscription behavior в корзине, checkout и benefit-программах.

## Итоговое влияние на backlog Best Ori Gin
- Нужна отдельная пользовательская feature для программы преимуществ партнера, а не только `кошелек` и не только `MLM`.
- Нужна отдельная админская feature для настройки loyalty/retention программ, чтобы не смешивать их с MLM compensation plan.
- Нужна более явная фиксация personal referral/social-selling механики.
- Нужно усилить описание cart/checkout на тему резерва, подписок и использования кошелька.
- Нужно усилить описание campaign management на тему 21-дневного цикла, rollover и maintenance window.
