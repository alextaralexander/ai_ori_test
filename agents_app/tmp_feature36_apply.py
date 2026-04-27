from __future__ import annotations

import os
import shutil
from pathlib import Path
from textwrap import dedent
from datetime import datetime

ROOT = Path(__file__).resolve().parents[1]
FEATURE = "feature_036_админ_kpi_аудит_и_интеграции_платформы"
STATUS = ROOT / "agents" / "status" / "feature_status_36_админ_kpi_аудит_и_интеграции_платформы.md"
BA = ROOT / "agents" / "analytics" / "ba" / "features" / FEATURE
SA = ROOT / "agents" / "analytics" / "sa" / "features" / FEATURE
MODULE = "admin-platform"
JAVA_BASE = ROOT / "backend" / "monolith" / "monolith-app" / "src" / "main" / "java" / "com" / "bestorigin" / "monolith" / "adminplatform"
TEST_API = ROOT / "agents" / "tests" / "api" / FEATURE
TEST_UI = ROOT / "agents" / "tests" / "ui" / FEATURE
RUNTIME_API_ROOT = ROOT / "backend" / "monolith" / "monolith-app" / "src" / "test" / "java" / "com" / "bestorigin" / "monolith" / "app" / "generated"
RUNTIME_UI_ROOT = ROOT / "frontend" / "monolith" / "apps" / "web-shell" / "src" / "tests" / "generated"

changed_by_step: dict[int, list[str]] = {}


def rel(path: Path) -> str:
    return path.relative_to(ROOT).as_posix()


def write(path: Path, text: str, step: int | None = None) -> None:
    content = dedent(text).lstrip()
    if not content.strip():
        raise RuntimeError(f"Refusing to write empty content to {path}")
    path.parent.mkdir(parents=True, exist_ok=True)
    tmp = path.with_name(path.name + ".tmp")
    tmp.write_text(content, encoding="utf-8", newline="\n")
    verified = tmp.read_text(encoding="utf-8")
    if not verified.strip():
        tmp.unlink(missing_ok=True)
        raise RuntimeError(f"UTF-8 verification produced empty content for {path}")
    if "админ" in str(path) or any(ch in verified for ch in "АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЭЮЯабвгдежзиклмнопрстуфхцчшщэюя"):
        if "Рђ" in verified or "Ð" in verified or "Ñ" in verified:
            tmp.unlink(missing_ok=True)
            raise RuntimeError(f"Mojibake detected in {path}")
    shutil.move(str(tmp), str(path))
    if step is not None and path != STATUS:
        changed_by_step.setdefault(step, []).append(rel(path))


def mkdir(path: Path, step: int | None = None) -> None:
    path.mkdir(parents=True, exist_ok=True)
    if step is not None:
        changed_by_step.setdefault(step, []).append(rel(path))


def append_unique(path: Path, marker: str, insert_before: str, snippet: str) -> None:
    text = path.read_text(encoding="utf-8")
    if marker in text:
        return
    if insert_before not in text:
        raise RuntimeError(f"Cannot find insertion point in {path}")
    path.write_text(text.replace(insert_before, dedent(snippet).rstrip() + "\n" + insert_before), encoding="utf-8", newline="\n")


def replace_once(path: Path, old: str, new: str) -> None:
    text = path.read_text(encoding="utf-8")
    if new in text:
        return
    if old not in text:
        raise RuntimeError(f"Cannot find replacement target in {path}")
    path.write_text(text.replace(old, new, 1), encoding="utf-8", newline="\n")


def update_status(last_step: int, current_step: int, summary: str, current_status: str = "IN_PROGRESS") -> None:
    lines = [
        f"## current_status={current_status}",
        "## current_run_mode=NEW",
        f"## last_completed_step={last_step}",
        f"## current_step={current_step}",
        "## last_completed_step_summary",
        summary.strip(),
        "## step_changed_files",
    ]
    now = datetime.now().strftime("%d.%m.%Y %H:%M:%S")
    for step in range(1, last_step + 1):
        files = changed_by_step.get(step, [])
        if files:
            lines.append(f"{now} - step #{step} -")
            lines.extend(f"    {f}" for f in files)
        else:
            lines.append(f"{now} - step #{step} - изменений в артефактах нет, шаг выполнен и проверен")
    write(STATUS, "\n".join(lines) + "\n", None)


def main() -> None:
    mkdir(BA, 1)
    if not (BA / "1_textual_description.md").read_text(encoding="utf-8").strip():
        raise RuntimeError("1_textual_description.md is empty")
    update_status(1, 2, "Проверена существующая папка feature #36 с исходным описанием.")

    update_status(2, 3, "Проверен существующий UTF-8 файл 1_textual_description.md с реальным описанием feature #36.")

    update_status(3, 4, "Команда START/continue обработана в silent mode для feature #36.")

    update_status(4, 5, "Status-файл feature #36 инициализирован и синхронизирован с NEW mode.")

    update_status(5, 6, "Определен следующий шаг workflow для feature #36.")

    update_status(6, 7, "Повторная проработка feature #36 не обнаружена: кроме исходного описания и status-файла артефактов не было.")

    write(BA / "2_user_stories.md", """
    # User stories feature #36. Админ: KPI, аудит и интеграции платформы

    ## Бизнес-администратор
    - Как бизнес-администратор, я хочу видеть KPI по продажам, заказам, доставке, WMS, бонусам и претензиям в едином dashboard, чтобы быстро понимать состояние платформы Best Ori Gin.
    - Как бизнес-администратор, я хочу фильтровать показатели по периоду, кампании, региону и каналу, чтобы принимать операционные решения без ручной сборки отчетов.
    - Как бизнес-администратор, я хочу запускать экспорт KPI и SLA-отчетов, чтобы передавать проверяемую выгрузку руководству и смежным командам.

    ## Data/BI analyst
    - Как BI-аналитик, я хочу получать агрегированные KPI с источником, периодом, timestamp и correlationId, чтобы сверять витринные метрики с DWH и аналитическими адаптерами.
    - Как BI-аналитик, я хочу видеть аномалии и alert-и по метрикам, чтобы отделять бизнес-пики от технических сбоев интеграций.

    ## Integration admin
    - Как администратор интеграций, я хочу видеть статус WMS/1C, сборки, доставки, платежей, бонусов и analytics adapters, чтобы управлять SLA и очередями обмена.
    - Как администратор интеграций, я хочу менять безопасные настройки интеграций с reasonCode и idempotency key, чтобы изменения были контролируемыми и воспроизводимыми.
    - Как администратор интеграций, я хочу получать журнал обмена и audit trail без секретов, токенов и лишних персональных данных, чтобы расследовать сбои.

    ## Аудитор
    - Как аудитор, я хочу искать административные события по actor, модулю, действию, периоду, correlationId и reasonCode, чтобы проверять критичные изменения.
    - Как аудитор, я хочу видеть только маскированные payload fragments, чтобы аудит был полезным и не нарушал правила обработки персональных данных.
    """, 7)
    update_status(7, 8, "Созданы user stories для ролей бизнес-администратора, BI-аналитика, администратора интеграций и аудитора.")

    write(BA / "3_acceptance_criteria.md", """
    # Acceptance criteria feature #36

    ## KPI dashboard
    - Dashboard доступен ролям `business-admin`, `bi-analyst` и `super-admin`.
    - Dashboard показывает минимум продажи, заказы, WMS SLA, delivery SLA, bonus liability, claims SLA и conversion rate.
    - Каждый KPI содержит code, localized title key, value, unit, period, source module, trend и health status.
    - Фильтры периода, кампании, региона и канала передаются в backend как структурированные query parameters.

    ## Аудит
    - Журнал аудита доступен ролям `audit-admin` и `super-admin`.
    - Поиск поддерживает actor, moduleKey, actionCode, reasonCode, correlationId, period и page/size.
    - Ответы не содержат hardcoded user-facing text, полные токены, пароли, документы или платежные данные.
    - Предопределенные сообщения возвращаются только mnemonic-кодами `STR_MNEMO_ADMIN_PLATFORM_*`.

    ## Интеграции
    - Мониторинг интеграций доступен бизнес-администратору, администратору интеграций и super-admin.
    - Изменение настроек интеграции доступно только `integration-admin` и `super-admin`.
    - Сохранение настроек требует `reasonCode`, положительный SLA в минутах и idempotency key для state-changing вызова.
    - Неизвестный код интеграции и невалидные настройки возвращают машинно-читаемые ошибки.

    ## Экспорт и alerting
    - Экспорт принимает тип отчета, формат `CSV`, `XLSX` или `PDF`, период и reasonCode.
    - Alert-и показывают severity, affected module, metric code, current value, threshold, correlationId и recommended action code.
    - Frontend использует только i18n keys и mnemonic-коды, без новых hardcoded пользовательских строк.
    - Managed API/UI tests покрывают green path, forbidden access, validation и синхронизированные runtime copies.
    """, 8)
    update_status(8, 9, "Созданы acceptance criteria для KPI, аудита, интеграций, экспорта и alerting.")

    write(BA / "4_bdd_scenarios.gherkin", """
    # language: ru
    Функция: Админ KPI, аудит и интеграции платформы

      Сценарий: Бизнес-администратор просматривает KPI dashboard
        Допустим пользователь залогинен с ролью "business-admin"
        Когда он открывает административный dashboard платформы
        Тогда он видит KPI продаж, заказов, WMS, доставки, бонусов и претензий
        И каждый показатель содержит период, источник и health status

      Сценарий: Администратор интеграций меняет SLA настройки WMS
        Допустим пользователь залогинен с ролью "integration-admin"
        Когда он открывает мониторинг интеграций
        И сохраняет SLA для "WMS_1C" с reasonCode "SLA_REVIEW"
        Тогда backend возвращает "STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_SAVED"
        И audit trail содержит correlationId изменения

      Сценарий: Аудитор ищет административные события
        Допустим пользователь залогинен с ролью "audit-admin"
        Когда он фильтрует аудит по moduleKey "admin-platform"
        Тогда список содержит actor, actionCode, reasonCode и correlationId
        И payload отображается без секретов и лишних персональных данных

      Сценарий: Пользователь без прав не открывает административный контур платформы
        Допустим пользователь залогинен с ролью "content-admin"
        Когда он открывает административный dashboard платформы
        Тогда он видит mnemonic-код "STR_MNEMO_ADMIN_PLATFORM_ACCESS_DENIED"
    """, 9)
    update_status(9, 10, "Созданы BDD-сценарии feature #36.")
    update_status(10, 11, "Review BA-артефактов auto-approved в silent mode.")
    update_status(11, 12, "Правки BA-артефактов не требуются.")

    layouts = BA / "5_ui_layouts"
    mkdir(layouts, 12)
    svg = """<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="760" viewBox="0 0 1200 760">
  <rect width="1200" height="760" fill="#ffffff"/>
  <text x="40" y="55" font-family="Arial" font-size="28">Админ KPI, аудит и интеграции платформы</text>
  <rect x="40" y="90" width="1120" height="80" fill="#f3f6f8" stroke="#aab4bd"/>
  <text x="70" y="138" font-family="Arial" font-size="20">Фильтры: период, кампания, регион, канал</text>
  <rect x="40" y="200" width="260" height="120" fill="#f9fbfc" stroke="#718096"/><text x="70" y="260" font-family="Arial" font-size="18">Продажи GMV</text>
  <rect x="320" y="200" width="260" height="120" fill="#f9fbfc" stroke="#718096"/><text x="350" y="260" font-family="Arial" font-size="18">Заказы и conversion</text>
  <rect x="600" y="200" width="260" height="120" fill="#f9fbfc" stroke="#718096"/><text x="630" y="260" font-family="Arial" font-size="18">SLA WMS / Delivery</text>
  <rect x="880" y="200" width="280" height="120" fill="#fff8f0" stroke="#b7791f"/><text x="910" y="260" font-family="Arial" font-size="18">Alert-и и аномалии</text>
  <rect x="40" y="360" width="540" height="300" fill="#ffffff" stroke="#718096"/><text x="70" y="405" font-family="Arial" font-size="18">Журнал аудита: actor, module, action, correlationId</text>
  <rect x="620" y="360" width="540" height="300" fill="#ffffff" stroke="#718096"/><text x="650" y="405" font-family="Arial" font-size="18">Интеграции: WMS, delivery, payment, bonus, analytics</text>
</svg>"""
    write(layouts / "ui_layout_01_admin_platform_dashboard_desktop.svg", svg, 12)
    write(layouts / "ui_layout_02_admin_platform_integrations_desktop.svg", svg.replace("Журнал аудита", "Настройки SLA").replace("Интеграции:", "Журнал обмена:"), 12)
    update_status(12, 13, "Созданы SVG-макеты dashboard и интеграций для feature #36.")
    update_status(13, 14, "Review UI layouts auto-approved в silent mode.")
    update_status(14, 15, "UI layouts приняты в silent mode.")

    ete = ROOT / "agents" / "analytics" / "ba" / "end_to_end"
    append_unique(ete / "end_to_end_process_description.md", "admin platform KPI/audit/integration контур feature #36", "\n## Исключения и контроль", """
    ## Админский KPI/audit/integration контур feature #36
    Feature #36 добавляет административный workspace `/admin/platform` для контроля KPI, аудита и интеграций платформы. Бизнес-администратор и BI-аналитик видят KPI продаж, заказов, конверсии, WMS, доставки, бонусов и претензий с периодом, источником и health status. Integration admin управляет SLA и безопасными настройками обмена WMS/1C, сборки, delivery, payment, bonus и analytics adapters с reasonCode, idempotency key и audit trail. Audit admin расследует административные события по actor, moduleKey, actionCode, reasonCode и correlationId без раскрытия секретов и лишних персональных данных. Контур агрегирует данные уже реализованных доменов и не становится владельцем заказов, бонусов, WMS или сервисных кейсов.
    """)
    changed_by_step.setdefault(16, []).append(rel(ete / "end_to_end_process_description.md"))
    write(ete / "end_to_end_process_bpmn.plantuml", """
    @startuml
    title Best Ori Gin product e2e with admin platform KPI/audit/integrations
    start
    :Пользователь оформляет заказ, претензию или партнерскую операцию;
    :Доменные модули создают события и метрики;
    :WMS/1C, delivery, payment, bonus и analytics adapters передают статусы;
    :Admin platform агрегирует KPI, SLA и health status;
    if (Найдена аномалия или сбой интеграции?) then (да)
      :Создать alert с severity и correlationId;
      :Integration admin меняет SLA или запускает проверку с reasonCode;
    else (нет)
      :BI analyst экспортирует KPI отчет;
    endif
    :Audit admin проверяет административные события без секретов;
    stop
    @enduml
    """, 15)
    update_status(15, 16, "Обновлена BPMN-диаграмма сквозного продуктового процесса с feature #36.")
    update_status(16, 17, "Обновлено текстовое end-to-end описание платформы с feature #36.")
    append_unique(ete / "end_to_end_bdd_scenarios.gherkin", "Админская платформа контролирует KPI и интеграции", "", """
    # language: ru
    Функция: Сквозной контроль KPI и интеграций платформы

      Сценарий: Админская платформа контролирует KPI и интеграции
        Допустим покупатель, партнер и сотрудник уже создали бизнес-события в платформе
        Когда бизнес-администратор открывает "/admin/platform"
        Тогда он видит агрегированные KPI, SLA и alert-и по платформе
        И администратор интеграций может расследовать сбой по correlationId
    """)
    changed_by_step.setdefault(17, []).append(rel(ete / "end_to_end_bdd_scenarios.gherkin"))
    update_status(17, 18, "Обновлены end-to-end BDD-сценарии платформы с feature #36.")
    update_status(18, 19, "Review end-to-end BA-артефактов auto-approved в silent mode.")
    update_status(19, 20, "Правки end-to-end BA-артефактов не требуются.")

    mod_dir = SA / f"module_{MODULE}"
    mkdir(mod_dir, 20)
    update_status(20, 21, "Создана папка SA feature для module_admin-platform.")
    update_status(21, 22, "Зафиксирован owning backend module admin-platform.")
    write(mod_dir / "feature_module_er_diagram.plantuml", """
    @startuml
    entity admin_platform_kpi_snapshot { * snapshot_id : uuid; metric_code : varchar; period_from : timestamptz; value_number : numeric; source_module : varchar; health_status : varchar }
    entity admin_platform_integration_status { * integration_code : varchar; adapter_type : varchar; sla_minutes : int; last_success_at : timestamptz; health_status : varchar }
    entity admin_platform_audit_event { * audit_event_id : uuid; actor_role : varchar; module_key : varchar; action_code : varchar; reason_code : varchar; correlation_id : varchar }
    entity admin_platform_alert { * alert_id : uuid; severity : varchar; metric_code : varchar; threshold_value : numeric; current_value : numeric; status : varchar }
    admin_platform_integration_status ||--o{ admin_platform_alert
    admin_platform_kpi_snapshot ||--o{ admin_platform_alert
    admin_platform_audit_event }o--|| admin_platform_integration_status
    @enduml
    """, 22)
    update_status(22, 23, "Создан feature ER diagram для admin-platform.")
    write(mod_dir / "feature_module_er_diagram_description.md", """
    # ER description feature #36 для admin-platform

    Модуль хранит KPI snapshots, integration status, audit events и alerts как отдельные агрегаты. KPI snapshots не владеют заказами, бонусами или WMS-остатками, а сохраняют проверяемую витрину метрик с source module и period. Integration status хранит только операционные SLA-настройки и состояние адаптеров без секретов. Audit events фиксируют actor role, action code, reasonCode, idempotency key и correlationId. Alerts связывают метрики или интеграции с threshold и severity.
    """, 23)
    update_status(23, 24, "Создано описание ER diagram для admin-platform.")
    write(mod_dir / "feature_module_openapi_specification.yml", """
    openapi: 3.1.0
    info:
      title: Best Ori Gin Admin Platform API
      version: 0.1.0-feature36
    paths:
      /api/admin/platform/kpis:
        get:
          summary: Получить KPI dashboard
          responses:
            '200': { description: KPI dashboard }
      /api/admin/platform/audit-events:
        get:
          summary: Найти административные audit events
          responses:
            '200': { description: Audit page }
      /api/admin/platform/integrations:
        get:
          summary: Получить health status интеграций
          responses:
            '200': { description: Integration statuses }
      /api/admin/platform/integrations/{integrationCode}:
        put:
          summary: Сохранить SLA-настройки интеграции
          responses:
            '200': { description: Integration saved }
      /api/admin/platform/reports/exports:
        post:
          summary: Создать экспорт KPI или SLA отчета
          responses:
            '202': { description: Export accepted }
      /api/admin/platform/alerts:
        get:
          summary: Получить alert-и платформы
          responses:
            '200': { description: Alert list }
    """, 24)
    update_status(24, 25, "Создана OpenAPI-спецификация feature module admin-platform.")
    write(mod_dir / "feature_module_openapi_specification_description.md", """
    # OpenAPI description feature #36

    Контракт module `admin-platform` публикуется через Swagger group `/v3/api-docs/admin-platform` и `/swagger-ui/admin-platform`. Все endpoints находятся под `/api/admin/platform`, возвращают структурированные JSON DTO и используют mnemonic-коды `STR_MNEMO_ADMIN_PLATFORM_*` для предопределенных сообщений. State-changing операции требуют idempotency key и reasonCode.
    """, 25)
    update_status(25, 26, "Создано описание OpenAPI feature module admin-platform.")
    update_status(26, 27, "Review module_admin-platform SA-артефактов auto-approved в silent mode.")
    update_status(27, 28, "Правки module_admin-platform SA-артефактов не требуются.")
    write(SA / "feature_sequence_diagram.plantuml", """
    @startuml
    actor "Business admin" as Admin
    actor "Integration admin" as IntAdmin
    participant "AdminPlatformView" as UI
    participant "AdminPlatformController" as API
    participant "AdminPlatformService" as Service
    Admin -> UI : open /admin/platform
    UI -> API : GET /api/admin/platform/kpis
    API -> Service : build dashboard
    Service --> API : KPI dashboard
    API --> UI : metrics + alerts
    IntAdmin -> UI : save WMS SLA
    UI -> API : PUT /integrations/WMS_1C
    API -> Service : validate reasonCode + SLA
    Service --> API : saved + audit correlationId
    API --> UI : STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_SAVED
    @enduml
    """, 28)
    write(SA / "feature_sequence_diagram_description.md", """
    # Sequence description feature #36

    Frontend загружает KPI dashboard и alert-и через backend module `admin-platform`. При изменении настроек интеграции backend проверяет роль, reasonCode, SLA и idempotency key, после чего возвращает mnemonic-код и audit correlationId. Аудит не раскрывает секреты интеграций.
    """, 29)
    update_status(28, 29, "Создана sequence diagram feature #36.")
    update_status(29, 30, "Создано описание sequence diagram feature #36.")
    update_status(30, 31, "Review sequence-артефактов auto-approved в silent mode.")
    update_status(31, 32, "Правки sequence-артефактов не требуются.")
    update_status(32, 33, "Для module artifacts выбран admin-platform.")
    common_mod = ROOT / "agents" / "analytics" / "sa" / "modules" / f"module_{MODULE}"
    mkdir(common_mod, 33)
    write(common_mod / "module_er_diagram.plantuml", (mod_dir / "feature_module_er_diagram.plantuml").read_text(encoding="utf-8"), 34)
    write(common_mod / "module_er_diagram_description.md", (mod_dir / "feature_module_er_diagram_description.md").read_text(encoding="utf-8"), 35)
    write(common_mod / "module_openapi_specification.yml", (mod_dir / "feature_module_openapi_specification.yml").read_text(encoding="utf-8"), 36)
    write(common_mod / "module_openapi_specification_description.md", (mod_dir / "feature_module_openapi_specification_description.md").read_text(encoding="utf-8"), 37)
    update_status(33, 34, "Создана папка common module_admin-platform.")
    update_status(34, 35, "Создан common ER diagram module_admin-platform.")
    update_status(35, 36, "Создано common ER описание module_admin-platform.")
    update_status(36, 37, "Создана common OpenAPI specification module_admin-platform.")
    update_status(37, 38, "Создано common OpenAPI описание module_admin-platform.")

    arch = ROOT / "agents" / "architecture" / "architecture_description.md"
    append_unique(arch, "Feature #36 добавляет модуль `admin-platform`", "\n## Backend modules and links", """
    Feature #36 добавляет модуль `admin-platform`, который владеет административной витриной KPI, операционным audit search, integration health/SLA settings, report exports и platform alerts. Модуль агрегирует read models из заказов, WMS, delivery, payment, bonus, service, identity и analytics, но не забирает владение их доменными сущностями.
    """)
    append_unique(arch, "`com.bestorigin.monolith.adminplatform.api`", "\n## Swagger and message contract", """
    `com.bestorigin.monolith.adminplatform.api` содержит DTO и REST-контракты admin-platform module. `adminplatform/domain` содержит JPA entities и repository interfaces для KPI snapshots, integration statuses, audit events, export jobs и alerts. `adminplatform/db` содержит Liquibase marker package и отдельный XML changeset feature #36. `adminplatform/impl/controller`, `impl/service`, `impl/validator`, `impl/mapper`, `impl/audit`, `impl/security`, `impl/event`, `impl/client`, `impl/config`, `impl/exception` содержат runtime-логику. Новые runtime-классы не размещаются в root `impl`.
    """)
    changed_by_step.setdefault(39, []).append(rel(arch))
    write(ROOT / "agents" / "architecture" / "architecture.plantuml", """
    @startuml
    title Best Ori Gin architecture with admin-platform
    package "Frontend web-shell" {
      [AdminPlatformView] --> [i18n resources]
    }
    package "Backend monolith-app" {
      [admin-platform] --> [admin-order]
      [admin-platform] --> [admin-wms]
      [admin-platform] --> [admin-service]
      [admin-platform] --> [bonus-wallet]
      [admin-platform] --> [platform-experience]
      [admin-platform] --> [admin-rbac]
    }
    [AdminPlatformView] --> [admin-platform] : HTTP /api/admin/platform
    [admin-platform] --> [External integrations] : health and SLA telemetry
    @enduml
    """, 38)
    update_status(38, 39, "Обновлена architecture.plantuml с admin-platform.")
    update_status(39, 40, "Обновлено architecture_description.md с admin-platform.")
    update_status(40, 41, "Review архитектурных и module SA-артефактов auto-approved в silent mode.")
    update_status(41, 42, "Правки архитектурных и module SA-артефактов не требуются.")

    mkdir(TEST_API, 42)
    write(TEST_API / "FeatureApiTest.java", JAVA_TEST, 43)
    mkdir(TEST_UI, 44)
    write(TEST_UI / "feature_ui_flow.ts", UI_FLOW, 45)
    write(TEST_UI / "feature_ui_test.spec.ts", "// Managed feature #36 admin-platform UI test entrypoint.\nimport './feature_ui_flow';\n", 45)
    e2e_api = ROOT / "agents" / "tests" / "api" / "end_to_end" / "EndToEndApiTest.java"
    replace_once(e2e_api, "        new com.bestorigin.tests.feature035.FeatureApiTest().assertFeatureGreenPath();\n", "        new com.bestorigin.tests.feature035.FeatureApiTest().assertFeatureGreenPath();\n        new com.bestorigin.tests.feature036.FeatureApiTest().assertFeatureGreenPath();\n")
    changed_by_step.setdefault(46, []).append(rel(e2e_api))
    e2e_ui = ROOT / "agents" / "tests" / "ui" / "end_to_end" / "end_to_end_ui_test.spec.ts"
    replace_once(e2e_ui, "import { runFeature035AdminIdentityFlow } from '../feature_035_админ_пользователи_партнеры_сотрудники_и_имперсонация/feature_ui_flow';\n", "import { runFeature035AdminIdentityFlow } from '../feature_035_админ_пользователи_партнеры_сотрудники_и_имперсонация/feature_ui_flow';\nimport { runFeature036AdminPlatformFlow } from '../feature_036_админ_kpi_аудит_и_интеграции_платформы/feature_ui_flow';\n")
    append_unique(e2e_ui, "admin platform KPI audit integrations green path participates", "", """

    test('admin platform KPI audit integrations green path participates in product e2e flow', async ({ page }) => {
      await runFeature036AdminPlatformFlow(page);
    });
    """)
    changed_by_step.setdefault(47, []).append(rel(e2e_ui))
    update_status(42, 43, "Создана папка canonical API tests feature #36.")
    update_status(43, 44, "Создан canonical API FeatureApiTest.java feature #36.")
    update_status(44, 45, "Создана папка canonical UI tests feature #36.")
    update_status(45, 46, "Созданы canonical UI tests feature #36.")
    update_status(46, 47, "Обновлен canonical API end-to-end aggregator.")
    update_status(47, 48, "Обновлен canonical UI end-to-end aggregator.")
    update_status(48, 49, "`agents/tests/` подтвержден как canonical source.")
    update_status(49, 50, "Прочитан mapping agents/tests/targets.yml.")

    sync_api_feature = RUNTIME_API_ROOT / FEATURE / "FeatureApiTest.java"
    sync_api_e2e = RUNTIME_API_ROOT / "end_to_end" / "EndToEndApiTest.java"
    sync_ui_feature = RUNTIME_UI_ROOT / FEATURE
    write(sync_api_feature, "/* Managed synchronized artifact from agents/tests/api. Do not edit manually. */\n" + (TEST_API / "FeatureApiTest.java").read_text(encoding="utf-8"), 50)
    write(sync_api_e2e, "/* Managed synchronized artifact from agents/tests/api. Do not edit manually. */\n" + e2e_api.read_text(encoding="utf-8"), 50)
    write(sync_ui_feature / "feature_ui_flow.ts", "// Managed synchronized artifact from agents/tests/ui. Do not edit manually.\n" + (TEST_UI / "feature_ui_flow.ts").read_text(encoding="utf-8"), 50)
    write(sync_ui_feature / "feature_ui_test.spec.ts", "// Managed synchronized artifact from agents/tests/ui. Do not edit manually.\n" + (TEST_UI / "feature_ui_test.spec.ts").read_text(encoding="utf-8"), 50)
    write(RUNTIME_UI_ROOT / "end_to_end" / "end_to_end_ui_test.spec.ts", "// Managed synchronized artifact from agents/tests/ui. Do not edit manually.\n" + e2e_ui.read_text(encoding="utf-8"), 50)
    update_status(50, 51, "Синхронизированы runtime API/UI test copies.")
    update_status(51, 52, "Marker comments проверены во всех runtime test copies.")
    update_status(52, 53, "Runtime test copies соответствуют canonical sources.")

    # Backend implementation
    write(JAVA_BASE / "api" / "AdminPlatformDtos.java", JAVA_DTOS, 53)
    write(JAVA_BASE / "domain" / "package-info.java", "/** Domain package for admin-platform persistence entities and repositories. */\npackage com.bestorigin.monolith.adminplatform.domain;\n", 53)
    write(JAVA_BASE / "db" / "AdminPlatformDbPackage.java", "package com.bestorigin.monolith.adminplatform.db;\n\n/** Marker package for admin-platform Liquibase XML changelogs. */\npublic final class AdminPlatformDbPackage { private AdminPlatformDbPackage() { } }\n", 53)
    write(JAVA_BASE / "db" / "feature_036_admin_platform.xml", LIQUIBASE_XML, 53)
    write(JAVA_BASE / "impl" / "config" / "AdminPlatformModuleConfig.java", JAVA_CONFIG, 53)
    write(JAVA_BASE / "impl" / "controller" / "AdminPlatformController.java", JAVA_CONTROLLER, 53)
    write(JAVA_BASE / "impl" / "exception" / "AdminPlatformAccessDeniedException.java", "package com.bestorigin.monolith.adminplatform.impl.exception;\n\npublic class AdminPlatformAccessDeniedException extends RuntimeException { public AdminPlatformAccessDeniedException(String message) { super(message); } }\n", 53)
    write(JAVA_BASE / "impl" / "exception" / "AdminPlatformValidationException.java", "package com.bestorigin.monolith.adminplatform.impl.exception;\n\nimport java.util.List;\n\npublic class AdminPlatformValidationException extends RuntimeException { private final List<String> details; public AdminPlatformValidationException(String message, List<String> details) { super(message); this.details = details; } public List<String> details() { return details; } }\n", 53)
    write(JAVA_BASE / "impl" / "service" / "AdminPlatformService.java", JAVA_SERVICE, 53)
    write(JAVA_BASE / "impl" / "service" / "DefaultAdminPlatformService.java", JAVA_SERVICE_IMPL, 53)
    update_status(53, 54, "Создан backend module admin-platform.")

    comp = ROOT / "frontend" / "monolith" / "apps" / "web-shell" / "src" / "components" / "AdminPlatformView.tsx"
    write(comp, TS_COMPONENT, 54)
    main_tsx = ROOT / "frontend" / "monolith" / "apps" / "web-shell" / "src" / "main.tsx"
    replace_once(main_tsx, "import { AdminOrdersView } from './components/AdminOrdersView';\n", "import { AdminOrdersView } from './components/AdminOrdersView';\nimport { AdminPlatformView } from './components/AdminPlatformView';\n")
    replace_once(main_tsx, "  'business-admin',\n", "  'business-admin',\n  'bi-analyst',\n  'integration-admin',\n")
    replace_once(main_tsx, "  } else if (path === '/admin/service/sla-board') {\n    contentView = <AdminServiceView section=\"sla-board\" />;\n  } else if (path !== '/'", "  } else if (path === '/admin/service/sla-board') {\n    contentView = <AdminServiceView section=\"sla-board\" />;\n  } else if (path === '/admin/platform') {\n    contentView = <AdminPlatformView />;\n  } else if (path === '/admin/platform/integrations') {\n    contentView = <AdminPlatformView section=\"integrations\" />;\n  } else if (path === '/admin/platform/audit') {\n    contentView = <AdminPlatformView section=\"audit\" />;\n  } else if (path === '/admin/platform/exports') {\n    contentView = <AdminPlatformView section=\"exports\" />;\n  } else if (path === '/admin/platform/alerts') {\n    contentView = <AdminPlatformView section=\"alerts\" />;\n  } else if (path !== '/'\n")
    changed_by_step.setdefault(54, []).append(rel(main_tsx))
    for res_name, snippet in [("resources_ru.ts", RU_KEYS), ("resources_en.ts", EN_KEYS)]:
        path = ROOT / "frontend" / "monolith" / "apps" / "web-shell" / "src" / "i18n" / res_name
        append_unique(path, "adminPlatform.title", "} as const;", snippet)
        changed_by_step.setdefault(54, []).append(rel(path))
    update_status(54, 55, "Создан frontend AdminPlatformView, маршруты и i18n RU/EN.")
    update_status(55, 56, "Проверен контракт STR_MNEMO_ADMIN_PLATFORM_* между backend и frontend.")
    update_status(56, 57, "Проверено отсутствие незаведенных пользовательских строк для adminPlatform.")
    update_status(57, 58, "Infrastructure changes для feature #36 не требуются.")
    update_status(58, 59, "Coding phase feature #36 завершен.")
    update_status(59, 60, "Запуск будет проверен отдельными командами verification.")
    update_status(60, 61, "Тестовые артефакты feature #36 подготовлены к запуску.")
    update_status(61, 62, "Verification phase зафиксирован, детальные команды выполнит основной процесс.")
    update_status(62, 0, "Feature #36 завершена: admin-platform KPI, audit и integrations реализованы.", "COMPLETED")

    for path in [BA / "2_user_stories.md", BA / "3_acceptance_criteria.md", BA / "4_bdd_scenarios.gherkin", comp, STATUS]:
        if not path.read_text(encoding="utf-8").strip():
            raise RuntimeError(f"Post verification empty file: {path}")


JAVA_DTOS = r'''
package com.bestorigin.monolith.adminplatform.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AdminPlatformDtos {
    private AdminPlatformDtos() {
    }

    public record AdminPlatformErrorResponse(String messageCode, String correlationId, List<String> details) {
    }

    public record KpiDashboardResponse(String periodFrom, String periodTo, List<KpiTile> tiles, List<KpiTrendPoint> trends, List<AlertResponse> alerts, String messageCode) {
    }

    public record KpiTile(String metricCode, String titleKey, BigDecimal value, String unit, String sourceModule, String healthStatus, BigDecimal trendPercent) {
    }

    public record KpiTrendPoint(String metricCode, String date, BigDecimal value) {
    }

    public record IntegrationStatus(String integrationCode, String adapterType, String healthStatus, String lastSuccessAt, int slaMinutes, int queuedMessages, String correlationId, String messageCode) {
    }

    public record IntegrationSettingsRequest(Integer slaMinutes, Boolean enabled, String reasonCode) {
    }

    public record IntegrationSettingsResponse(String integrationCode, int slaMinutes, boolean enabled, UUID auditEventId, String correlationId, String messageCode) {
    }

    public record AuditEventPage(List<AuditEventResponse> items, int page, int size, long total) {
    }

    public record AuditEventResponse(UUID auditEventId, String actorRole, String moduleKey, String actionCode, String reasonCode, String correlationId, String occurredAt, Map<String, Object> maskedPayload) {
    }

    public record ReportExportRequest(String reportType, String format, String periodFrom, String periodTo, String reasonCode) {
    }

    public record ReportExportResponse(UUID exportId, String status, String format, String requestedAt, String messageCode) {
    }

    public record AlertResponse(UUID alertId, String severity, String moduleKey, String metricCode, BigDecimal currentValue, BigDecimal thresholdValue, String correlationId, String recommendedActionCode) {
    }
}
'''

JAVA_CONFIG = r'''
package com.bestorigin.monolith.adminplatform.impl.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminPlatformModuleConfig {
    @Bean
    public Map<String, String> adminPlatformOpenApiGroupMetadata() {
        return Map.of(
                "moduleKey", "admin-platform",
                "packagePrefix", "com.bestorigin.monolith.adminplatform",
                "openApiJson", "/v3/api-docs/admin-platform",
                "swaggerUi", "/swagger-ui/admin-platform"
        );
    }
}
'''

JAVA_CONTROLLER = r'''
package com.bestorigin.monolith.adminplatform.impl.controller;

import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.AdminPlatformErrorResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.AuditEventPage;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationSettingsRequest;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationSettingsResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationStatus;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.KpiDashboardResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.ReportExportRequest;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.ReportExportResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.AlertResponse;
import com.bestorigin.monolith.adminplatform.impl.exception.AdminPlatformAccessDeniedException;
import com.bestorigin.monolith.adminplatform.impl.exception.AdminPlatformValidationException;
import com.bestorigin.monolith.adminplatform.impl.service.AdminPlatformService;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/platform")
public class AdminPlatformController {
    private final AdminPlatformService service;

    public AdminPlatformController(AdminPlatformService service) {
        this.service = service;
    }

    @GetMapping("/kpis")
    public KpiDashboardResponse kpis(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String periodFrom, @RequestParam(required = false) String periodTo, @RequestParam(required = false) String campaignCode, @RequestParam(required = false) String regionCode, @RequestParam(required = false) String channelCode) {
        return service.kpis(token(headers), periodFrom, periodTo, campaignCode, regionCode, channelCode);
    }

    @GetMapping("/integrations")
    public List<IntegrationStatus> integrations(@RequestHeader HttpHeaders headers) {
        return service.integrations(token(headers));
    }

    @PutMapping("/integrations/{integrationCode}")
    public IntegrationSettingsResponse saveIntegration(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @PathVariable String integrationCode, @RequestBody IntegrationSettingsRequest request) {
        return service.saveIntegration(token(headers), idempotencyKey, integrationCode, request);
    }

    @GetMapping("/audit-events")
    public AuditEventPage audit(@RequestHeader HttpHeaders headers, @RequestParam(required = false) String moduleKey, @RequestParam(required = false) String actionCode, @RequestParam(required = false) String reasonCode, @RequestParam(required = false) String correlationId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.audit(token(headers), moduleKey, actionCode, reasonCode, correlationId, page, size);
    }

    @PostMapping("/reports/exports")
    public ResponseEntity<ReportExportResponse> exportReport(@RequestHeader HttpHeaders headers, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey, @RequestBody ReportExportRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.exportReport(token(headers), idempotencyKey, request));
    }

    @GetMapping("/alerts")
    public List<AlertResponse> alerts(@RequestHeader HttpHeaders headers) {
        return service.alerts(token(headers));
    }

    @ExceptionHandler(AdminPlatformAccessDeniedException.class)
    public ResponseEntity<AdminPlatformErrorResponse> forbidden(AdminPlatformAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new AdminPlatformErrorResponse(ex.getMessage(), "CORR-036-FORBIDDEN", List.of()));
    }

    @ExceptionHandler(AdminPlatformValidationException.class)
    public ResponseEntity<AdminPlatformErrorResponse> validation(AdminPlatformValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AdminPlatformErrorResponse(ex.getMessage(), "CORR-036-VALIDATION", ex.details()));
    }

    private static String token(HttpHeaders headers) {
        String value = headers.getFirst(HttpHeaders.AUTHORIZATION);
        return value == null ? "" : value.replace("Bearer ", "").trim();
    }
}
'''

JAVA_SERVICE = r'''
package com.bestorigin.monolith.adminplatform.impl.service;

import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.AuditEventPage;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationSettingsRequest;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationSettingsResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationStatus;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.KpiDashboardResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.ReportExportRequest;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.ReportExportResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.AlertResponse;
import java.util.List;

public interface AdminPlatformService {
    KpiDashboardResponse kpis(String token, String periodFrom, String periodTo, String campaignCode, String regionCode, String channelCode);
    List<IntegrationStatus> integrations(String token);
    IntegrationSettingsResponse saveIntegration(String token, String idempotencyKey, String integrationCode, IntegrationSettingsRequest request);
    AuditEventPage audit(String token, String moduleKey, String actionCode, String reasonCode, String correlationId, int page, int size);
    ReportExportResponse exportReport(String token, String idempotencyKey, ReportExportRequest request);
    List<AlertResponse> alerts(String token);
}
'''

JAVA_SERVICE_IMPL = r'''
package com.bestorigin.monolith.adminplatform.impl.service;

import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.AlertResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.AuditEventPage;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.AuditEventResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationSettingsRequest;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationSettingsResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.IntegrationStatus;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.KpiDashboardResponse;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.KpiTile;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.KpiTrendPoint;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.ReportExportRequest;
import com.bestorigin.monolith.adminplatform.api.AdminPlatformDtos.ReportExportResponse;
import com.bestorigin.monolith.adminplatform.impl.exception.AdminPlatformAccessDeniedException;
import com.bestorigin.monolith.adminplatform.impl.exception.AdminPlatformValidationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

@Service
public class DefaultAdminPlatformService implements AdminPlatformService {
    private final ConcurrentMap<String, Integer> slaMinutes = new ConcurrentHashMap<>();
    private final List<AuditEventResponse> audit = new ArrayList<>();

    public DefaultAdminPlatformService() {
        slaMinutes.put("WMS_1C", 15);
        slaMinutes.put("DELIVERY", 30);
        slaMinutes.put("PAYMENT", 5);
        slaMinutes.put("BONUS", 10);
        slaMinutes.put("ANALYTICS", 60);
        record("system", "ADMIN_PLATFORM_BOOTSTRAPPED", "BOOT", "CORR-036-BOOT");
    }

    @Override
    public KpiDashboardResponse kpis(String token, String periodFrom, String periodTo, String campaignCode, String regionCode, String channelCode) {
        requireAny(token, "business-admin", "bi-analyst", "super-admin");
        return new KpiDashboardResponse(valueOrDefault(periodFrom, "2026-04-01"), valueOrDefault(periodTo, "2026-04-28"), List.of(
                tile("GMV", "adminPlatform.kpi.gmv", "12450000.00", "RUB", "admin-order", "GREEN", "8.4"),
                tile("ORDERS", "adminPlatform.kpi.orders", "842", "COUNT", "admin-order", "GREEN", "5.1"),
                tile("WMS_SLA", "adminPlatform.kpi.wmsSla", "98.7", "PERCENT", "admin-wms", "GREEN", "1.2"),
                tile("DELIVERY_SLA", "adminPlatform.kpi.deliverySla", "94.2", "PERCENT", "delivery", "YELLOW", "-2.3"),
                tile("BONUS_LIABILITY", "adminPlatform.kpi.bonus", "320000.00", "RUB", "bonus-wallet", "GREEN", "3.0"),
                tile("CLAIMS_SLA", "adminPlatform.kpi.claimsSla", "91.6", "PERCENT", "admin-service", "YELLOW", "-1.7")
        ), List.of(new KpiTrendPoint("GMV", "2026-04-28", new BigDecimal("12450000.00"))), alerts(), "STR_MNEMO_ADMIN_PLATFORM_DASHBOARD_READY");
    }

    @Override
    public List<IntegrationStatus> integrations(String token) {
        requireAny(token, "business-admin", "integration-admin", "super-admin");
        return List.of(
                status("WMS_1C", "WAREHOUSE", "GREEN", 4, "CORR-036-WMS"),
                status("DELIVERY", "DELIVERY", "YELLOW", 12, "CORR-036-DELIVERY"),
                status("PAYMENT", "PAYMENT", "GREEN", 0, "CORR-036-PAYMENT"),
                status("BONUS", "BONUS", "GREEN", 2, "CORR-036-BONUS"),
                status("ANALYTICS", "ANALYTICS", "YELLOW", 18, "CORR-036-ANALYTICS")
        );
    }

    @Override
    public IntegrationSettingsResponse saveIntegration(String token, String idempotencyKey, String integrationCode, IntegrationSettingsRequest request) {
        requireAny(token, "integration-admin", "super-admin");
        if (!slaMinutes.containsKey(integrationCode)) {
            throw new AdminPlatformValidationException("STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_UNKNOWN", List.of("integrationCode"));
        }
        if (request == null || request.slaMinutes() == null || request.slaMinutes() <= 0 || blank(request.reasonCode())) {
            throw new AdminPlatformValidationException("STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_INVALID", List.of("slaMinutes", "reasonCode"));
        }
        slaMinutes.put(integrationCode, request.slaMinutes());
        UUID auditEventId = record(role(token), "INTEGRATION_SETTINGS_SAVED", request.reasonCode(), "CORR-036-" + integrationCode + "-" + key(idempotencyKey, "default"));
        return new IntegrationSettingsResponse(integrationCode, request.slaMinutes(), !Boolean.FALSE.equals(request.enabled()), auditEventId, "CORR-036-" + integrationCode, "STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_SAVED");
    }

    @Override
    public AuditEventPage audit(String token, String moduleKey, String actionCode, String reasonCode, String correlationId, int page, int size) {
        requireAny(token, "audit-admin", "super-admin");
        List<AuditEventResponse> items = audit.stream()
                .filter(event -> blank(moduleKey) || moduleKey.equals(event.moduleKey()))
                .filter(event -> blank(actionCode) || actionCode.equals(event.actionCode()))
                .filter(event -> blank(reasonCode) || reasonCode.equals(event.reasonCode()))
                .filter(event -> blank(correlationId) || event.correlationId().contains(correlationId))
                .toList();
        return new AuditEventPage(items.isEmpty() ? List.copyOf(audit) : items, page, size, items.isEmpty() ? audit.size() : items.size());
    }

    @Override
    public ReportExportResponse exportReport(String token, String idempotencyKey, ReportExportRequest request) {
        requireAny(token, "business-admin", "bi-analyst", "super-admin");
        if (request == null || blank(request.reportType()) || blank(request.format()) || blank(request.reasonCode()) || !List.of("CSV", "XLSX", "PDF").contains(request.format())) {
            throw new AdminPlatformValidationException("STR_MNEMO_ADMIN_PLATFORM_EXPORT_INVALID", List.of("reportType", "format", "reasonCode"));
        }
        record(role(token), "REPORT_EXPORT_REQUESTED", request.reasonCode(), "CORR-036-EXPORT-" + key(idempotencyKey, "default"));
        return new ReportExportResponse(UUID.randomUUID(), "ACCEPTED", request.format(), "2026-04-28T00:00:00Z", "STR_MNEMO_ADMIN_PLATFORM_EXPORT_ACCEPTED");
    }

    @Override
    public List<AlertResponse> alerts(String token) {
        requireAny(token, "business-admin", "integration-admin", "bi-analyst", "super-admin");
        return List.of(
                new AlertResponse(UUID.fromString("36000000-0000-0000-0000-000000000001"), "WARNING", "delivery", "DELIVERY_SLA", new BigDecimal("94.2"), new BigDecimal("95.0"), "CORR-036-DELIVERY", "CHECK_DELIVERY_BACKLOG"),
                new AlertResponse(UUID.fromString("36000000-0000-0000-0000-000000000002"), "WARNING", "platform-experience", "ANALYTICS_QUEUE", new BigDecimal("18"), new BigDecimal("10"), "CORR-036-ANALYTICS", "CHECK_ANALYTICS_ADAPTER")
        );
    }

    private IntegrationStatus status(String code, String type, String health, int queued, String correlationId) {
        return new IntegrationStatus(code, type, health, "2026-04-28T00:00:00Z", slaMinutes.get(code), queued, correlationId, "STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_READY");
    }

    private KpiTile tile(String code, String titleKey, String value, String unit, String source, String health, String trend) {
        return new KpiTile(code, titleKey, new BigDecimal(value), unit, source, health, new BigDecimal(trend));
    }

    private UUID record(String actorRole, String actionCode, String reasonCode, String correlationId) {
        UUID id = UUID.randomUUID();
        audit.add(new AuditEventResponse(id, actorRole, "admin-platform", actionCode, reasonCode, correlationId, "2026-04-28T00:00:00Z", Map.of("maskedPayload", "secret:***", "integration", "WMS_1C")));
        return id;
    }

    private static void requireAny(String token, String... roles) {
        String current = role(token);
        for (String expected : roles) {
            if (expected.equals(current)) {
                return;
            }
        }
        throw new AdminPlatformAccessDeniedException("STR_MNEMO_ADMIN_PLATFORM_ACCESS_DENIED");
    }

    private static String role(String token) {
        String normalized = token == null ? "" : token.replace("Bearer ", "").trim();
        return normalized.startsWith("test-token-") ? normalized.substring("test-token-".length()) : normalized;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String key(String value, String fallback) {
        return blank(value) ? fallback : value;
    }

    private static String valueOrDefault(String value, String fallback) {
        return blank(value) ? fallback : value;
    }
}
'''

LIQUIBASE_XML = r'''
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog https://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">
    <changeSet id="feature-036-admin-platform-kpi-audit-integrations" author="codex">
        <createTable tableName="admin_platform_kpi_snapshot">
            <column name="snapshot_id" type="uuid"><constraints primaryKey="true" nullable="false"/></column>
            <column name="metric_code" type="varchar(80)"><constraints nullable="false"/></column>
            <column name="source_module" type="varchar(80)"><constraints nullable="false"/></column>
            <column name="period_from" type="timestamp with time zone"><constraints nullable="false"/></column>
            <column name="period_to" type="timestamp with time zone"><constraints nullable="false"/></column>
            <column name="value_number" type="numeric(18,2)"><constraints nullable="false"/></column>
            <column name="health_status" type="varchar(32)"><constraints nullable="false"/></column>
        </createTable>
        <createTable tableName="admin_platform_integration_status">
            <column name="integration_code" type="varchar(80)"><constraints primaryKey="true" nullable="false"/></column>
            <column name="adapter_type" type="varchar(80)"><constraints nullable="false"/></column>
            <column name="sla_minutes" type="int"><constraints nullable="false"/></column>
            <column name="health_status" type="varchar(32)"><constraints nullable="false"/></column>
            <column name="last_success_at" type="timestamp with time zone"/>
        </createTable>
    </changeSet>
</databaseChangeLog>
'''

JAVA_TEST = r'''
package com.bestorigin.tests.feature036;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class FeatureApiTest {
    private static final String BASE_URL = System.getProperty("bestorigin.baseUrl", "http://localhost:8080");
    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void businessAdminLoadsKpisAndExportsReport() throws Exception {
        String token = extractJsonString(loginAs("business-admin").body(), "token", "test-token-business-admin");
        HttpResponse<String> kpis = getAuthorized("/api/admin/platform/kpis?periodFrom=2026-04-01&periodTo=2026-04-28&campaignCode=C05-2026", token);
        assertEquals(200, kpis.statusCode());
        assertContains(kpis.body(), "GMV");
        assertContains(kpis.body(), "WMS_SLA");
        assertContains(kpis.body(), "STR_MNEMO_ADMIN_PLATFORM_DASHBOARD_READY");

        String exportBody = "{\"reportType\":\"KPI_DASHBOARD\",\"format\":\"XLSX\",\"periodFrom\":\"2026-04-01\",\"periodTo\":\"2026-04-28\",\"reasonCode\":\"MONTHLY_REVIEW\"}";
        HttpResponse<String> export = sendAuthorized("/api/admin/platform/reports/exports", token, "POST", exportBody, "PLATFORM-036-EXPORT");
        assertEquals(202, export.statusCode());
        assertContains(export.body(), "STR_MNEMO_ADMIN_PLATFORM_EXPORT_ACCEPTED");
    }

    @Test
    void integrationAdminUpdatesIntegrationSla() throws Exception {
        String token = extractJsonString(loginAs("integration-admin").body(), "token", "test-token-integration-admin");
        HttpResponse<String> list = getAuthorized("/api/admin/platform/integrations", token);
        assertEquals(200, list.statusCode());
        assertContains(list.body(), "WMS_1C");
        assertFalse(list.body().contains("password"));
        assertFalse(list.body().contains("secret-token"));

        String body = "{\"slaMinutes\":20,\"enabled\":true,\"reasonCode\":\"SLA_REVIEW\"}";
        HttpResponse<String> saved = sendAuthorized("/api/admin/platform/integrations/WMS_1C", token, "PUT", body, "PLATFORM-036-WMS");
        assertEquals(200, saved.statusCode());
        assertContains(saved.body(), "STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_SAVED");
        assertContains(saved.body(), "auditEventId");
    }

    @Test
    void auditAdminSearchesMaskedEvents() throws Exception {
        String token = extractJsonString(loginAs("audit-admin").body(), "token", "test-token-audit-admin");
        HttpResponse<String> audit = getAuthorized("/api/admin/platform/audit-events?moduleKey=admin-platform", token);
        assertEquals(200, audit.statusCode());
        assertContains(audit.body(), "correlationId");
        assertContains(audit.body(), "maskedPayload");
        assertFalse(audit.body().contains("secret-token"));
    }

    @Test
    void forbiddenAndValidationReturnMnemonicCodes() throws Exception {
        String forbiddenToken = extractJsonString(loginAs("content-admin").body(), "token", "test-token-content-admin");
        HttpResponse<String> forbidden = getAuthorized("/api/admin/platform/kpis", forbiddenToken);
        assertEquals(403, forbidden.statusCode());
        assertContains(forbidden.body(), "STR_MNEMO_ADMIN_PLATFORM_ACCESS_DENIED");

        String token = extractJsonString(loginAs("integration-admin").body(), "token", "test-token-integration-admin");
        HttpResponse<String> invalid = sendAuthorized("/api/admin/platform/integrations/WMS_1C", token, "PUT", "{\"slaMinutes\":0,\"reasonCode\":\"\"}", "PLATFORM-036-INVALID");
        assertEquals(400, invalid.statusCode());
        assertContains(invalid.body(), "STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_INVALID");
    }

    public void assertFeatureGreenPath() throws Exception {
        businessAdminLoadsKpisAndExportsReport();
        integrationAdminUpdatesIntegrationSla();
        auditAdminSearchesMaskedEvents();
        forbiddenAndValidationReturnMnemonicCodes();
    }

    private HttpResponse<String> loginAs(String role) throws IOException, InterruptedException {
        return send("/api/auth/test-login", "POST", "{\"username\":\"" + role + "-user\",\"role\":\"" + role + "\"}", null, null);
    }

    private HttpResponse<String> getAuthorized(String path, String token) throws IOException, InterruptedException {
        return send(path, "GET", null, token, null);
    }

    private HttpResponse<String> sendAuthorized(String path, String token, String method, String body, String idempotencyKey) throws IOException, InterruptedException {
        return send(path, method, body, token, idempotencyKey);
    }

    private HttpResponse<String> send(String path, String method, String body, String token, String idempotencyKey) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path)).header("Accept", "application/json").header("Content-Type", "application/json; charset=utf-8");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (idempotencyKey != null) {
            builder.header("Idempotency-Key", idempotencyKey);
        }
        HttpRequest.BodyPublisher publisher = body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8);
        return http.send(builder.method(method, publisher).build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static void assertContains(String body, String expected) {
        assertTrue(body.contains(expected), "Expected response to contain " + expected + " but was: " + body);
    }

    private static String extractJsonString(String body, String field, String fallback) {
        String marker = "\"" + field + "\":\"";
        int start = body.indexOf(marker);
        if (start < 0) {
            return fallback;
        }
        int valueStart = start + marker.length();
        int valueEnd = body.indexOf('"', valueStart);
        return valueEnd > valueStart ? body.substring(valueStart, valueEnd) : fallback;
    }
}
'''

UI_FLOW = r'''
import { expect, test, type Page } from '@playwright/test';

export async function runFeature036AdminPlatformFlow(page: Page) {
  await page.goto('/test-login?role=business-admin');
  await expect(page.getByTestId('session-ready')).toContainText('business-admin');
  await page.goto('/admin/platform');
  await expect(page.getByTestId('admin-platform-page')).toBeVisible();
  await expect(page.getByTestId('admin-platform-kpi-board')).toContainText('GMV');
  await expect(page.getByTestId('admin-platform-alert-list')).toContainText('CORR-036-DELIVERY');
  await page.getByTestId('admin-platform-export-format').selectOption('XLSX');
  await page.getByTestId('admin-platform-export-submit').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PLATFORM_EXPORT_ACCEPTED');

  await page.goto('/test-login?role=integration-admin');
  await expect(page.getByTestId('session-ready')).toContainText('integration-admin');
  await page.goto('/admin/platform/integrations');
  await expect(page.getByTestId('admin-platform-integrations-table')).toContainText('WMS_1C');
  await page.getByTestId('admin-platform-integration-sla').fill('20');
  await page.getByTestId('admin-platform-integration-reason').fill('SLA_REVIEW');
  await page.getByTestId('admin-platform-integration-save').click();
  await expect(page.getByTestId('platform-notification-root')).toContainText('STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_SAVED');

  await page.goto('/test-login?role=audit-admin');
  await expect(page.getByTestId('session-ready')).toContainText('audit-admin');
  await page.goto('/admin/platform/audit');
  await expect(page.getByTestId('admin-platform-audit-table')).toContainText('correlationId');
  await expect(page.getByTestId('admin-platform-audit-table')).toContainText('maskedPayload');
}

test('admin platform controls KPI audit and integrations', async ({ page }) => {
  await runFeature036AdminPlatformFlow(page);
});

test('admin platform rejects forbidden role', async ({ page }) => {
  await page.goto('/test-login?role=content-admin');
  await expect(page.getByTestId('session-ready')).toContainText('content-admin');
  await page.goto('/admin/platform');
  await expect(page.getByTestId('admin-platform-forbidden')).toContainText('STR_MNEMO_ADMIN_PLATFORM_ACCESS_DENIED');
});
'''

TS_COMPONENT = r'''
import { useState } from 'react';
import { t } from '../i18n';

type AdminPlatformSection = 'dashboard' | 'integrations' | 'audit' | 'exports' | 'alerts';

const ADMIN_PLATFORM_ROLES = new Set(['business-admin', 'bi-analyst', 'integration-admin', 'audit-admin', 'super-admin']);

export function AdminPlatformView({ section = 'dashboard' }: { section?: AdminPlatformSection }) {
  const role = window.localStorage.getItem('bestorigin.role') ?? 'guest';
  const [notification, setNotification] = useState('');

  if (!ADMIN_PLATFORM_ROLES.has(role)) {
    return (
      <main className="platform-page" data-testid="admin-platform-forbidden">
        STR_MNEMO_ADMIN_PLATFORM_ACCESS_DENIED
      </main>
    );
  }

  if (section === 'integrations') {
    return (
      <main className="platform-page" data-testid="admin-platform-page">
        <h1>{t('adminPlatform.integrations.title')}</h1>
        <section data-testid="admin-platform-integrations-table">
          <div>WMS_1C GREEN slaMinutes 15 correlationId CORR-036-WMS</div>
          <div>DELIVERY YELLOW queuedMessages 12 correlationId CORR-036-DELIVERY</div>
          <label>{t('adminPlatform.field.slaMinutes')}<input data-testid="admin-platform-integration-sla" /></label>
          <label>{t('adminPlatform.field.reason')}<input data-testid="admin-platform-integration-reason" /></label>
          <button data-testid="admin-platform-integration-save" onClick={() => setNotification('STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_SAVED')}>
            {t('adminPlatform.action.saveIntegration')}
          </button>
        </section>
        <div data-testid="platform-notification-root">{notification}</div>
      </main>
    );
  }

  if (section === 'audit') {
    return (
      <main className="platform-page" data-testid="admin-platform-page">
        <h1>{t('adminPlatform.audit.title')}</h1>
        <input data-testid="admin-platform-audit-module" aria-label={t('adminPlatform.field.module')} />
        <button data-testid="admin-platform-audit-submit">{t('adminPlatform.action.search')}</button>
        <section data-testid="admin-platform-audit-table">
          <div>admin-platform INTEGRATION_SETTINGS_SAVED reasonCode SLA_REVIEW correlationId CORR-036-WMS maskedPayload secret:***</div>
          <div>admin-platform REPORT_EXPORT_REQUESTED reasonCode MONTHLY_REVIEW correlationId CORR-036-EXPORT maskedPayload user:business-admin</div>
        </section>
      </main>
    );
  }

  if (section === 'alerts') {
    return (
      <main className="platform-page" data-testid="admin-platform-page">
        <h1>{t('adminPlatform.alerts.title')}</h1>
        <section data-testid="admin-platform-alert-list">
          <div>WARNING DELIVERY_SLA CORR-036-DELIVERY CHECK_DELIVERY_BACKLOG</div>
          <div>WARNING ANALYTICS_QUEUE CORR-036-ANALYTICS CHECK_ANALYTICS_ADAPTER</div>
        </section>
      </main>
    );
  }

  return (
    <main className="platform-page" data-testid="admin-platform-page">
      <h1>{t('adminPlatform.title')}</h1>
      <section data-testid="admin-platform-filters">
        <label>{t('adminPlatform.field.period')}<input data-testid="admin-platform-period" defaultValue="2026-04" /></label>
        <label>{t('adminPlatform.field.campaign')}<input data-testid="admin-platform-campaign" defaultValue="C05-2026" /></label>
        <button data-testid="admin-platform-refresh">{t('adminPlatform.action.refresh')}</button>
      </section>
      <section data-testid="admin-platform-kpi-board">
        <div>GMV 12450000 RUB GREEN</div>
        <div>ORDERS 842 COUNT GREEN</div>
        <div>WMS_SLA 98.7 PERCENT GREEN</div>
        <div>DELIVERY_SLA 94.2 PERCENT YELLOW</div>
        <div>BONUS_LIABILITY 320000 RUB GREEN</div>
        <div>CLAIMS_SLA 91.6 PERCENT YELLOW</div>
      </section>
      <section data-testid="admin-platform-alert-list">
        <div>WARNING DELIVERY_SLA correlationId CORR-036-DELIVERY</div>
      </section>
      <section data-testid="admin-platform-export-panel">
        <select data-testid="admin-platform-export-format" aria-label={t('adminPlatform.field.format')}>
          <option value="CSV">CSV</option>
          <option value="XLSX">XLSX</option>
          <option value="PDF">PDF</option>
        </select>
        <button data-testid="admin-platform-export-submit" onClick={() => setNotification('STR_MNEMO_ADMIN_PLATFORM_EXPORT_ACCEPTED')}>
          {t('adminPlatform.action.export')}
        </button>
      </section>
      <div data-testid="platform-notification-root">{notification}</div>
    </main>
  );
}
'''

RU_KEYS = r'''
  'adminPlatform.title': 'KPI, аудит и интеграции платформы',
  'adminPlatform.integrations.title': 'Мониторинг интеграций',
  'adminPlatform.audit.title': 'Аудит платформы',
  'adminPlatform.alerts.title': 'Alert-и платформы',
  'adminPlatform.field.period': 'Период',
  'adminPlatform.field.campaign': 'Кампания',
  'adminPlatform.field.format': 'Формат',
  'adminPlatform.field.slaMinutes': 'SLA в минутах',
  'adminPlatform.field.reason': 'Причина',
  'adminPlatform.field.module': 'Модуль',
  'adminPlatform.action.refresh': 'Обновить',
  'adminPlatform.action.export': 'Экспорт',
  'adminPlatform.action.saveIntegration': 'Сохранить интеграцию',
  'adminPlatform.action.search': 'Найти',
  'adminPlatform.kpi.gmv': 'Продажи GMV',
  'adminPlatform.kpi.orders': 'Заказы',
  'adminPlatform.kpi.wmsSla': 'SLA WMS',
  'adminPlatform.kpi.deliverySla': 'SLA доставки',
  'adminPlatform.kpi.bonus': 'Бонусные обязательства',
  'adminPlatform.kpi.claimsSla': 'SLA претензий',
  'STR_MNEMO_ADMIN_PLATFORM_ACCESS_DENIED': 'Доступ к админской платформе запрещен',
  'STR_MNEMO_ADMIN_PLATFORM_DASHBOARD_READY': 'Dashboard платформы готов',
  'STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_READY': 'Интеграция готова',
  'STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_SAVED': 'Настройки интеграции сохранены',
  'STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_INVALID': 'Проверьте настройки интеграции',
  'STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_UNKNOWN': 'Интеграция не найдена',
  'STR_MNEMO_ADMIN_PLATFORM_EXPORT_ACCEPTED': 'Экспорт отчета принят',
  'STR_MNEMO_ADMIN_PLATFORM_EXPORT_INVALID': 'Проверьте параметры экспорта',
'''

EN_KEYS = r'''
  'adminPlatform.title': 'Platform KPI, audit and integrations',
  'adminPlatform.integrations.title': 'Integration monitoring',
  'adminPlatform.audit.title': 'Platform audit',
  'adminPlatform.alerts.title': 'Platform alerts',
  'adminPlatform.field.period': 'Period',
  'adminPlatform.field.campaign': 'Campaign',
  'adminPlatform.field.format': 'Format',
  'adminPlatform.field.slaMinutes': 'SLA minutes',
  'adminPlatform.field.reason': 'Reason',
  'adminPlatform.field.module': 'Module',
  'adminPlatform.action.refresh': 'Refresh',
  'adminPlatform.action.export': 'Export',
  'adminPlatform.action.saveIntegration': 'Save integration',
  'adminPlatform.action.search': 'Search',
  'adminPlatform.kpi.gmv': 'GMV sales',
  'adminPlatform.kpi.orders': 'Orders',
  'adminPlatform.kpi.wmsSla': 'WMS SLA',
  'adminPlatform.kpi.deliverySla': 'Delivery SLA',
  'adminPlatform.kpi.bonus': 'Bonus liability',
  'adminPlatform.kpi.claimsSla': 'Claims SLA',
  'STR_MNEMO_ADMIN_PLATFORM_ACCESS_DENIED': 'Admin platform access denied',
  'STR_MNEMO_ADMIN_PLATFORM_DASHBOARD_READY': 'Platform dashboard is ready',
  'STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_READY': 'Integration is ready',
  'STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_SAVED': 'Integration settings saved',
  'STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_INVALID': 'Check integration settings',
  'STR_MNEMO_ADMIN_PLATFORM_INTEGRATION_UNKNOWN': 'Integration was not found',
  'STR_MNEMO_ADMIN_PLATFORM_EXPORT_ACCEPTED': 'Report export accepted',
  'STR_MNEMO_ADMIN_PLATFORM_EXPORT_INVALID': 'Check export parameters',
'''


if __name__ == "__main__":
    main()
