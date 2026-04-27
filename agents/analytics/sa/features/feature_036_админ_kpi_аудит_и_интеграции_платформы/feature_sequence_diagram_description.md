# Sequence description feature #36

Frontend открывает administrative platform workspace, получает KPI dashboard, затем при необходимости сохраняет настройки интеграции с idempotency key и reasonCode. Service проверяет роль, валидирует SLA/retry policy, фиксирует audit event и возвращает mnemonic-код без hardcoded UI-текста.