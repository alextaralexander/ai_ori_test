# Acceptance criteria. Feature 013. Профиль и настройки пользователя

## AC-013-01. Доступ и маршруты
- `/profile-settings`, `/profile-settings/general`, `/profile-settings/contacts`, `/profile-settings/addresses`, `/profile-settings/documents` и `/profile-settings/security` доступны только авторизованному пользователю.
- Пользователь видит только собственный профиль, кроме явно разрешенного support-сценария.
- Попытка открыть или изменить чужой профиль без support-контекста возвращает 403 и mnemonic `STR_MNEMO_PROFILE_ACCESS_DENIED`.
- Если профиль не найден, backend возвращает mnemonic `STR_MNEMO_PROFILE_NOT_FOUND`.

## AC-013-02. Обзор профиля
- Обзор профиля содержит summary по секциям `general`, `contacts`, `addresses`, `documents`, `security` и `readiness`.
- Для каждой секции возвращается machine-readable статус `COMPLETE`, `INCOMPLETE`, `REQUIRES_VERIFICATION` или `LOCKED`.
- Readiness показывает недостающие обязательные данные для checkout, delivery и claim flows.
- Empty или incomplete states возвращаются структурированно и не содержат hardcoded user-facing backend-текста.

## AC-013-03. Основные данные
- Пользователь может обновить имя, фамилию, отчество при наличии, дату рождения, пол и preferred language.
- Обязательные поля валидируются до сохранения, даты не могут быть в будущем.
- Сохранение одной секции не перезаписывает данные других секций.
- Успешное изменение критичных полей фиксируется в истории изменений.

## AC-013-04. Контакты
- Пользователь может добавить, изменить, удалить телефон или email и назначить основной контакт каждого типа.
- В профиле не может быть двух основных телефонов или двух основных email одновременно.
- Подтвержденный контакт возвращается с verification status и masked value.
- Повторная отправка операции с тем же idempotency key не создает дублирующий контакт.

## AC-013-05. Подтверждение контактов
- Backend поддерживает запуск подтверждения телефона или email и возврат machine-readable статуса процесса.
- Нельзя пометить контакт подтвержденным без успешной проверки.
- Ошибки истекшего, неверного или слишком часто запрошенного подтверждения возвращаются mnemonic-кодами `STR_MNEMO_*`.
- Frontend локализует все публичные сообщения подтверждения через i18n dictionaries.

## AC-013-06. Адресная книга
- Пользователь может создавать, редактировать, удалять и назначать адрес по умолчанию.
- Адрес содержит structured fields: страна, регион, город, улица, дом, корпус при наличии, квартира/офис при наличии, индекс, комментарий доставки.
- Нельзя удалить адрес, который заблокирован активным order, delivery или claim flow; backend возвращает machine reason и mnemonic.
- Checkout и claim flows получают единый default address и readiness status из profile module.

## AC-013-07. Документы
- Пользователь может сохранить документные данные, необходимые для доставки, возврата, партнерских операций или юридически значимых сценариев.
- Document payload хранится и возвращается с masked fields; полный номер документа не попадает во frontend после сохранения.
- Нельзя создать два активных документа одного типа, если бизнес-правило допускает только один active document.
- Изменение документа всегда фиксируется в audit trail как критичное изменение.

## AC-013-08. Security-настройки
- Пользователь может сменить пароль только после проверки текущего пароля или другого разрешенного security challenge.
- Новый пароль должен пройти backend-валидацию сложности и не совпадать с текущим паролем.
- Security view показывает последние критичные события, active sessions или trusted devices в структурированном виде, если такие данные доступны в текущем baseline.
- Предопределенные причины отказа возвращаются только mnemonic-кодами `STR_MNEMO_*`.

## AC-013-09. История критичных изменений
- История содержит событие, секцию, field key, masked old/new value при применимости, actor type, source channel, business reason и occurredAt.
- Пользователь видит только события собственного профиля.
- Оператор поддержки видит историю только при наличии разрешенного support-контекста, и сам просмотр фиксируется в audit trail.
- В истории не раскрываются полные document numbers, full tokens, пароли или секреты.

## AC-013-10. Support-сценарий
- Support-роль может открыть профиль пользователя только с audit context и обязательным reason.
- Support-изменения ограничены разрешенными полями и не обходят бизнес-валидации.
- Каждый support-просмотр и каждое изменение сохраняют actor id, reason, source channel и признак `auditRecorded=true`.
- Public explanations для пользователя передаются во frontend через mnemonic-коды.

## AC-013-11. Backend-frontend contract
- Backend возвращает profile DTOs со структурированными секциями, validation errors, machine reasons, masked values, allowed actions и mnemonic-кодами.
- Backend не передает hardcoded user-facing текст в API responses, validation payloads или error payloads.
- Все mnemonic-коды, которые могут попасть во frontend, добавлены во все поддерживаемые frontend dictionaries.
- API поддерживает идемпотентность для операций добавления контакта, адреса и документа.

## AC-013-12. Frontend i18n
- Все новые заголовки, labels, подсказки, placeholders, ошибки, success-сообщения, empty states, statuses и CTA определены в `resources_ru.ts` и `resources_en.ts`.
- React-компоненты не содержат новых hardcoded user-facing строк.
- Для route metadata и form validation messages используются i18n keys.
- Устаревшие ключи удалены, если соответствующий текст больше не используется.

## AC-013-13. UI и доступность
- Все маршруты профиля имеют стабильные `data-testid` для managed UI tests.
- Редактирование секций доступно с клавиатуры, validation messages связаны с полями, а успешные/ошибочные состояния объявляются через доступные status regions.
- На мобильной ширине навигация по секциям не требует горизонтальной прокрутки страницы.
- Формы не теряют введенные данные при ошибке backend-валидации.

## AC-013-14. Тесты и синхронизация
- Managed API test покрывает обзор профиля, обновление general data, contacts, addresses, documents, password change, forbidden access и support audit.
- Managed UI test покрывает навигацию по разделам, сохранение секции, validation error, readiness warnings и security flow.
- End-to-end managed tests агрегируют реальный FeatureApiTest и реальный feature UI flow, а не placeholder-assertions.
- Runtime-копии тестов создаются только из `agents/tests/` с marker comment.

## AC-013-15. Liquibase и пакетная структура
- Dedicated Liquibase XML для feature #13 создан в changelog profile и не смешан с changelog других фич.
- Backend DTO размещаются в `profile/api`, JPA entities и repositories в `profile/domain`, changelog в `profile/db`, runtime orchestration в `profile/impl` с role-specific subpackages.
- Новые backend runtime classes не размещаются напрямую в root `impl`.
- Все новые автоматизированные Java test classes заканчиваются суффиксом `Test`.
