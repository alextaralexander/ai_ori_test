# Feature module OpenAPI description. Employee

OpenAPI feature #19 описывает dedicated employee group `/v3/api-docs/employee` и runtime base path `/api/employee`. Контракт разделен на три пользовательских сценария: workspace search, operator order и order support. Все ответы об ошибках используют mnemonic-коды `STR_MNEMO_EMPLOYEE_*`, которые frontend локализует через словари.

Контракт не передает hardcoded user-facing сообщения из backend во frontend. Персональные данные возвращаются в маскированном виде. Для изменяющих операций применяется `Idempotency-Key`, чтобы повторный клик сотрудника или retry не создавал второй заказ, заметку, корректировку или эскалацию.