# Module bonus-wallet OpenAPI description

Полная спецификация module `bonus-wallet` включает summary, transaction search, transaction details, order apply limits, export metadata, finance view и manual adjustments. Контроллеры размещаются в `com.bestorigin.monolith.bonuswallet.impl.controller`, поэтому runtime Swagger monolith должен отдавать группу `/v3/api-docs/bonus-wallet` и UI `/swagger-ui/bonus-wallet`.

Все predefined пользовательские сообщения передаются mnemonic-кодами `STR_MNEMO_*`, а frontend решает их через i18n dictionaries.
