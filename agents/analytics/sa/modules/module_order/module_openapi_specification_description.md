# Module order OpenAPI specification description

Module `order` предоставляет checkout, order history, repeat order и claims API. Feature #12 добавляет claim-create, claim-history, claim-details and claim-comments resources под `/api/order/claims`. Все предопределенные пользовательские сообщения возвращаются mnemonic-кодами `STR_MNEMO_*`; frontend локализует их через dictionaries.

Swagger/OpenAPI в monolith генерируется runtime через springdoc для module key `order`, а этот YAML фиксирует целевой SA-контракт.